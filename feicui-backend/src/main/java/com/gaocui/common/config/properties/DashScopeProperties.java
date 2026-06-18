package com.gaocui.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里 DashScope(通义千问) 配置 (前缀 dashscope).
 * chatModel 文本模型(找货匹配), visionModel 视觉模型(发布商品识图).
 */
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    private String apiKey;
    private String visionModel;
    private String chatModel;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getVisionModel() {
        return visionModel;
    }

    public void setVisionModel(String visionModel) {
        this.visionModel = visionModel;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }
}
