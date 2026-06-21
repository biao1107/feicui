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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品向量知识库 (RAG).
 * 用 DashScope text-embedding 把已上架商品文本化后存入内存向量库;
 * 找货时按买家需求语义检索 top-K 作为候选, 替代"全量塞 prompt".
 *
 * <p>持久化: embedding 落 t_product_embedding, 启动直接从库加载(不调 DashScope),
 * 避免 DashScope 抖动导致启动失败; 上下架触发 rebuild() 时重算并落库(增量 upsert).</p>
 */
@Slf4j
@Component
public class ProductKnowledgeBase implements ApplicationRunner {

    private static final int TOP_K = 8;
    private static final String META_PRODUCT_ID = "productId";

    private final ProductMapper productMapper;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final InMemoryEmbeddingStore<TextSegment> store;

    public ProductKnowledgeBase(EmbeddingModel embeddingModel, ProductMapper productMapper, JdbcTemplate jdbcTemplate) {
        this.embeddingModel = embeddingModel;
        this.productMapper = productMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.store = new InMemoryEmbeddingStore<>();
    }

    @Override
    public void run(ApplicationArguments args) {
        loadFromDb();
    }

    /**
     * 启动: 从 t_product_embedding 加载(不调 DashScope); 与当前 LISTED 商品对齐,
     * 全部命中则免调 API, 否则全量重建并落库.
     */
    private void loadFromDb() {
        List<Product> listed = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Product.STATUS_LISTED));
        if (listed.isEmpty()) {
            log.info("[RAG] 无已上架商品, 跳过");
            return;
        }
        Set<Long> listedIds = listed.stream().map(Product::getId).collect(Collectors.toSet());
        Map<Long, float[]> cached = new HashMap<>();
        jdbcTemplate.query("SELECT product_id, embedding FROM t_product_embedding", rs -> {
            long pid = rs.getLong("product_id");
            if (!listedIds.contains(pid)) {
                return;
            }
            byte[] bytes = rs.getBytes("embedding");
            if (bytes != null && bytes.length % 4 == 0) {
                cached.put(pid, toFloats(bytes));
            }
        });
        int hit = 0;
        for (Product p : listed) {
            float[] vec = cached.get(p.getId());
            if (vec != null) {
                store.add(String.valueOf(p.getId()), Embedding.from(vec), toSegment(p));
                hit++;
            }
        }
        if (hit == listed.size()) {
            log.info("[RAG] 从持久化加载 {} 件(免调用 DashScope)", hit);
        } else {
            log.info("[RAG] 持久化命中 {}/{}, 全量重建并落库", hit, listed.size());
            rebuild();
        }
    }

    /** 全量重建: 清空内存 → 重算 embedding → 填内存 + 落库(upsert). 上下架后调用 */
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
        List<String> ids = listed.stream().map(p -> String.valueOf(p.getId())).toList();
        store.addAll(ids, embeddings, segments);
        for (int i = 0; i < listed.size(); i++) {
            upsertEmbedding(listed.get(i).getId(), embeddings.get(i).vector());
        }
        log.info("[RAG] 商品知识库已重建并持久化, 共 {} 件", listed.size());
    }

    /**
     * 增量更新: 单商品上架/编辑 → 只算该条 embedding(不全量重算), upsert 内存库 + 持久化表.
     * 用 productId 作内存库 id, 保证幂等更新; 商品非上架状态则转为移除.
     */
    public void upsert(Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null || !Product.STATUS_LISTED.equals(p.getStatus())) {
            remove(productId);
            return;
        }
        TextSegment seg = toSegment(p);
        Embedding emb = embeddingModel.embed(seg).content();
        store.removeAll(List.of(String.valueOf(productId)));
        store.add(String.valueOf(productId), emb, seg);
        upsertEmbedding(productId, emb.vector());
        log.info("[RAG] 增量更新商品 {} 向量", productId);
    }

    /** 增量移除: 单商品下架/删除 → 从内存库 + 持久化表移除 */
    public void remove(Long productId) {
        store.removeAll(List.of(String.valueOf(productId)));
        jdbcTemplate.update("DELETE FROM t_product_embedding WHERE product_id = ?", productId);
        log.info("[RAG] 移除商品 {} 向量", productId);
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

    // ==================== embedding 持久化序列化(little-endian float[]) ====================

    private void upsertEmbedding(Long productId, float[] vector) {
        jdbcTemplate.update(
                "INSERT INTO t_product_embedding(product_id, embedding) VALUES(?, ?) " +
                        "ON DUPLICATE KEY UPDATE embedding = VALUES(embedding)",
                productId, toBytes(vector));
    }

    private static byte[] toBytes(float[] v) {
        ByteBuffer bb = ByteBuffer.allocate(v.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : v) {
            bb.putFloat(f);
        }
        return bb.array();
    }

    private static float[] toFloats(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        float[] v = new float[b.length / 4];
        for (int i = 0; i < v.length; i++) {
            v[i] = bb.getFloat();
        }
        return v;
    }
}
