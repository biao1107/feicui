package com.gaocui.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.modules.product.entity.Product;
import com.gaocui.modules.product.mapper.ProductMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;

/**
 * 商品向量知识库 (RAG).
 * 用 DashScope text-embedding 把已上架商品文本化后存入内存向量库;
 * 找货时按买家需求语义检索 top-K 作为候选, 替代"全量塞 prompt".
 * 内存库重启丢失, 启动时全量重建; 商品上下架后调用 rebuild() 同步.
 */
@Component
public class ProductKnowledgeBase implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductKnowledgeBase.class);

    /** 每次检索返回的候选数 (再由 LLM 从中精选最多3个) */
    private static final int TOP_K = 8;
    private static final String META_PRODUCT_ID = "productId";

    private final ProductMapper productMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;

    public ProductKnowledgeBase(EmbeddingModel embeddingModel, ProductMapper productMapper) {
        this.productMapper = productMapper;
        this.embeddingModel = embeddingModel;
        this.store = new InMemoryEmbeddingStore<>();
    }

    @Override
    public void run(ApplicationArguments args) {
        rebuild();
    }

    /** 全量重建: 清空并重新 ingest 所有已上架商品 */
    public void rebuild() {
        List<Product> listed = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Product.STATUS_LISTED));
        store.removeAll();
        if (listed.isEmpty()) {
            log.info("[RAG] 商品知识库为空, 跳过重建");
            return;
        }
        List<TextSegment> segments = listed.stream().map(this::toSegment).toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(embeddings, segments);
        log.info("[RAG] 商品知识库已重建, 共 {} 件", listed.size());
    }

    /** 语义检索 top-K 相关商品的 id(按相似度降序) */
    public List<Long> search(String query) {
        Embedding q = embeddingModel.embed(query).content();
        EmbeddingSearchResult<TextSegment> res = store.search(
                new EmbeddingSearchRequest(q, TOP_K, 0.0, null));
        return res.matches().stream()
                .map(EmbeddingMatch::embedded)
                .map(seg -> seg.metadata().getLong(META_PRODUCT_ID))
                .filter(Objects::nonNull)
                .toList();
    }

    /** 商品 → 文本段(标题+标签+简介+价格), metadata 存 productId 供检索回填 */
    private TextSegment toSegment(Product p) {
        StringBuilder sb = new StringBuilder();
        if (p.getTitle() != null) {
            sb.append(p.getTitle());
        }
        if (p.getTags() != null && !p.getTags().isEmpty()) {
            sb.append(" 标签:").append(String.join(",", p.getTags()));
        }
        if (p.getBrief() != null && !p.getBrief().isBlank()) {
            sb.append(" ").append(p.getBrief());
        }
        if (p.getPrice() != null) {
            sb.append(" 价格约").append(p.getPrice()).append("元");
        }
        return TextSegment.from(sb.toString(), new Metadata().put(META_PRODUCT_ID, p.getId()));
    }
}
