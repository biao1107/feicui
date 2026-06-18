package com.gaocui.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.config.properties.DashScopeProperties;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.modules.ai.dto.AiGenerateResponse;
import com.gaocui.modules.ai.dto.AiMatchResponse;
import com.gaocui.modules.ai.dto.ProductCard;
import com.gaocui.modules.product.entity.Product;
import com.gaocui.modules.product.mapper.ProductMapper;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 服务 (LangChain4j + 阿里 DashScope).
 * 1. 找货匹配: 文本模型解析买家需求, 从已上架货源中匹配最多3件.
 * 2. 图片转文案: 视觉模型识别翡翠图片, 生成标题/简介/详情/标签/估价.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 非翡翠需求的固定回复 */
    private static final String NON_JADE_REPLY =
            "我是高翠AI，专业找翡翠助手。您可以试着这样问：\n- 找5万左右的冰种翡翠\n- 我要A货，没预算上限";

    private final DashScopeProperties props;
    private final ProductMapper productMapper;
    private final QwenChatModel chatModel;
    private final QwenChatModel visionModel;

    public AiService(DashScopeProperties props, ProductMapper productMapper) {
        this.props = props;
        this.productMapper = productMapper;
        this.chatModel = QwenChatModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getChatModel())
                .build();
        this.visionModel = QwenChatModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getVisionModel())
                .build();
    }

    // ==================== 找货匹配 ====================
    public AiMatchResponse match(String message) {
        // 1. 取所有已上架商品作为货源库
        List<Product> listed = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Product.STATUS_LISTED));

        // 2. 构建货源库上下文(精简字段)
        String ctx = listed.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("tags", p.getTags());
            m.put("price", p.getPrice());
            return toRawJson(m);
        }).collect(Collectors.joining(","));

        // 3. 让 LLM 判断需求并选品
        String prompt = "你是高翠网的AI找货助手, 专帮买家匹配翡翠货源。\n"
                + "货源库(JSON数组): [" + ctx + "]\n"
                + "买家需求: \"" + message + "\"\n"
                + "请判断该需求是否为翡翠找货需求, 并从货源库选出最匹配的(最多3个)商品。\n"
                + "严格只返回如下JSON, 不要任何额外文字或```标记:\n"
                + "{\"isJadeNeed\":true或false,\"reply\":\"给买家的中文回复(匹配到则说明已找到N件优质货源)\","
                + "\"productIds\":[匹配的商品id, 最多3个]}\n"
                + "规则: 若非翡翠需求, isJadeNeed=false, reply固定为:" + NON_JADE_REPLY + ", productIds为空数组; "
                + "货源库为空时 productIds 为空数组。";

        JsonNode node = parseJsonObject(chat(prompt, chatModel));

        AiMatchResponse resp = new AiMatchResponse();
        boolean isJade = node.path("isJadeNeed").asBoolean(true);
        if (!isJade) {
            resp.setReply(NON_JADE_REPLY);
            resp.setProducts(List.of());
            return resp;
        }
        resp.setReply(node.path("reply").asText("已为您解析需求，正在匹配优质翡翠货源..."));

        // 4. 按 LLM 返回的 id 装配卡片
        List<Long> ids = new ArrayList<>();
        node.path("productIds").forEach(n -> {
            try {
                ids.add(n.asLong());
            } catch (Exception ignored) {
            }
        });
        List<ProductCard> cards = listed.stream()
                .filter(p -> ids.contains(p.getId()))
                .limit(3)
                .map(this::toCard)
                .toList();
        resp.setProducts(cards);
        return resp;
    }

    // ==================== 图片转商品文案 ====================
    public AiGenerateResponse generateProductInfo(List<String> images) {
        if (images == null || images.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "至少需要1张图片");
        }
        String imageUrl = images.get(0);
        String prompt = "你是翡翠商品文案专家。根据这张翡翠图片生成商品信息, 严格只返回如下JSON, 不要任何额外文字或```标记:\n"
                + "{\"title\":\"简洁商品标题\",\"brief\":\"50字以内简介\","
                + "\"description\":\"300字以内详情(种水/颜色/工艺/适合场景)\","
                + "\"price\":估价纯数字(单位元),\"tags\":[\"标签1\",\"标签2\",\"标签3\"]}";
        JsonNode node = parseJsonObject(vision(imageUrl, prompt));

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

    private String chat(String prompt, QwenChatModel model) {
        try {
            return model.chat(prompt);
        } catch (Exception e) {
            log.error("[AI] 文本调用失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
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

    private ProductCard toCard(Product p) {
        ProductCard c = new ProductCard();
        c.setId(p.getId());
        c.setTitle(p.getTitle());
        c.setTags(p.getTags());
        c.setPrice(p.getPrice());
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            c.setCoverImage(p.getImages().get(0));
        }
        return c;
    }
}
