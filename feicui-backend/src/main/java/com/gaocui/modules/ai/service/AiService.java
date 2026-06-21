package com.gaocui.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.config.properties.DashScopeProperties;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.modules.ai.dto.AiGenerateResponse;
import com.gaocui.modules.ai.dto.AiMatchResponse;
import com.gaocui.modules.ai.dto.MatchResult;
import com.gaocui.modules.ai.dto.ProductCard;
import com.gaocui.modules.merchant.entity.Merchant;
import com.gaocui.modules.merchant.mapper.MerchantMapper;
import com.gaocui.modules.product.entity.Product;
import com.gaocui.modules.product.mapper.ProductMapper;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 服务 (LangChain4j + 阿里 DashScope).
 * 1. 找货匹配: 文本模型解析买家需求, 从已上架货源中匹配最多3件.
 * 2. 图片转文案: 视觉模型识别翡翠图片, 生成标题/简介/详情/标签/估价.
 */
@Slf4j
@Service
public class AiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final ProductKnowledgeBase knowledgeBase;
    private final QwenChatModel visionModel;
    private final JadeMatchAssistant matchAssistant;
    private final Executor aiExecutor;
    private final DashScopeProperties dashScopeProperties;
    private final String generatePrompt;
    /** 非翡翠需求的固定回复(从 prompt/non-jade-reply.txt 加载) */
    private final String nonJadeReply;

    public AiService(ProductMapper productMapper, MerchantMapper merchantMapper,
                     ProductKnowledgeBase knowledgeBase,
                     @Qualifier("visionModel") QwenChatModel visionModel,
                     JadeMatchAssistant matchAssistant,
                     @Qualifier("aiExecutor") Executor aiExecutor,
                     DashScopeProperties dashScopeProperties) {
        this.productMapper = productMapper;
        this.merchantMapper = merchantMapper;
        this.knowledgeBase = knowledgeBase;
        this.visionModel = visionModel;
        this.matchAssistant = matchAssistant;
        this.aiExecutor = aiExecutor;
        this.dashScopeProperties = dashScopeProperties;
        this.generatePrompt = loadClasspath("prompt/generate-system.txt");
        this.nonJadeReply = loadClasspath("prompt/non-jade-reply.txt");
    }

    // ==================== 找货匹配 ====================

    /**
     * 异步找货匹配: 提交到独立 aiExecutor 线程池, 不阻塞 Tomcat 请求线程.
     * QwenChatModel 不暴露超时, 用 orTimeout 兜底, 保证用户侧在 timeoutSec 内必返回.
     * 异常(match 内 BusinessException / 超时 TimeoutException)统一由调用方 exceptionally 处理.
     */
    public CompletableFuture<AiMatchResponse> matchAsync(String message) {
        return CompletableFuture.supplyAsync(() -> match(message), aiExecutor)
                .orTimeout(dashScopeProperties.getTimeoutSec(), TimeUnit.SECONDS);
    }

    public AiMatchResponse match(String message) {
        // 1. RAG: 语义检索 top-K 相关商品作为候选(替代全量塞 prompt, 已按相似度排序)
        List<Product> candidates = loadCandidates(message);

        // 2. 标记 VIP 商家(供 prompt 选品倾斜与卡片标识)
        Set<Long> vipMerchantIds = vipMerchantIdsOf(candidates);

        // 3. 声明式调用: prompt 与结构化返回交给 LangChain4j AiServices (见 JadeMatchAssistant)
        MatchResult result = matchAssistant.match(message, buildContext(candidates, vipMerchantIds));

        AiMatchResponse resp = new AiMatchResponse();
        if (!result.isJadeNeed()) {
            resp.setReply(nonJadeReply);
            resp.setProducts(List.of());
            return resp;
        }
        resp.setReply(result.reply());

        // 4. 按模型返回的 id 顺序装配卡片
        Map<Long, Product> idMap = candidates.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        List<ProductCard> cards = result.productIds().stream()
                .map(idMap::get)
                .filter(Objects::nonNull)
                .limit(3)
                .map(p -> toCard(p, vipMerchantIds.contains(p.getMerchantId())))
                .toList();
        resp.setProducts(cards);
        return resp;
    }

    /** RAG 检索候选: 向量库 top-K 的 productId → 查 Product, 保持相似度顺序 */
    private List<Product> loadCandidates(String message) {
        List<Long> ids = knowledgeBase.search(message);
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> map = productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        return ids.stream().map(map::get).filter(Objects::nonNull).toList();
    }

    // ==================== 图片转商品文案 ====================
    public AiGenerateResponse generateProductInfo(List<String> images) {
        if (images == null || images.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "至少需要1张图片");
        }
        String imageUrl = images.get(0);
        JsonNode node = parseJsonObject(vision(imageUrl, generatePrompt));

        AiGenerateResponse resp = new AiGenerateResponse();
        resp.setTitle(textOf(node, "title"));
        resp.setBrief(textOf(node, "brief"));
        resp.setDescription(textOf(node, "description"));
        String priceStr = textOf(node, "price").replaceAll("[^0-9.]", "");
        if (!priceStr.isEmpty()) {
            resp.setPrice(new BigDecimal(priceStr));
        }
        List<String> tags = new ArrayList<>();
        node.path("tags").forEach(n -> tags.add(n.asText()));
        resp.setTags(tags);
        return resp;
    }

    // ==================== 内部工具 ====================

    /** 读取 classpath 资源文件(prompt 工程化: prompt 抽到 resources/prompt/*.txt) */
    private static String loadClasspath(String path) {
        try (var in = new ClassPathResource(path).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("无法加载 prompt 资源: " + path, e);
        }
    }

    private String vision(String imageUrl, String prompt) {
        try {
            UserMessage msg = UserMessage.from(
                    ImageContent.from(Image.builder().url(imageUrl).build()),
                    TextContent.from(prompt)
            );
            ChatResponse r = visionModel.chat(msg);
            return r.aiMessage().text();
        } catch (Exception e) {
            log.error("[AI] 视觉调用失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }
    }

    /** 从 LLM 返回文本中提取首个 JSON 对象 */
    private JsonNode parseJsonObject(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.startsWith("```")) {
            s = s.replaceAll("^```\\w*", "").replaceAll("```$", "").trim();
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end < start) {
            log.warn("[AI] 返回非JSON: {}", raw);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }
        try {
            return MAPPER.readTree(s.substring(start, end + 1));
        } catch (Exception e) {
            log.warn("[AI] JSON解析失败: {}", raw, e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }
    }

    private String textOf(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n == null ? null : n.asText();
    }

    private String toRawJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** 构建货源库上下文 JSON(精简字段, 含 isVip 标记) */
    private String buildContext(List<Product> listed, Set<Long> vipMerchantIds) {
        return listed.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("isVip", vipMerchantIds.contains(p.getMerchantId()));
            m.put("title", p.getTitle());
            m.put("tags", p.getTags());
            m.put("price", p.getPrice());
            return toRawJson(m);
        }).collect(Collectors.joining(","));
    }

    private ProductCard toCard(Product p, boolean vip) {
        ProductCard c = new ProductCard();
        c.setId(p.getId());
        c.setTitle(p.getTitle());
        c.setTags(p.getTags());
        c.setPrice(p.getPrice());
        c.setVip(vip);
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            c.setCoverImage(p.getImages().get(0));
        }
        return c;
    }

    /** 批量判定这些商品所属商家中, 当前生效为 VIP(未过期)的 merchantId 集合 */
    private Set<Long> vipMerchantIdsOf(List<Product> products) {
        Set<Long> merchantIds = products.stream()
                .map(Product::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Set.of();
        }
        return merchantMapper.selectBatchIds(merchantIds).stream()
                .filter(Merchant::isVipEffective)
                .map(Merchant::getId)
                .collect(Collectors.toSet());
    }
}
