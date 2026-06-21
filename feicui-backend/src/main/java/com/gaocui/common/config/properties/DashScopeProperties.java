package com.gaocui.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里 DashScope(通义千问) 配置 (前缀 dashscope).
 * chatModel 文本模型(找货匹配), visionModel 视觉模型(发布商品识图),
 * embeddingModel 向量模型(商品 RAG 检索).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    private String apiKey;
    private String visionModel;
    private String chatModel;
    /** 向量模型(商品 RAG 检索), 默认 text-embedding-v3 */
    private String embeddingModel = "text-embedding-v3";
}
