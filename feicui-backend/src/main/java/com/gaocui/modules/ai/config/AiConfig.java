package com.gaocui.modules.ai.config;

import com.gaocui.common.config.properties.DashScopeProperties;
import com.gaocui.modules.ai.service.JadeMatchAssistant;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 组件集中装配 (智能体工厂).
 * 模型与 AiServices 代理在此 @Bean 创建, 业务 Service 只注入使用;
 * 后续要加会话记忆/工具/RAG-retriever, 只在本类的 builder 上扩展, 不碰业务代码.
 */
@Configuration
public class AiConfig {

    @Bean
    public QwenChatModel chatModel(DashScopeProperties props) {
        return QwenChatModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getChatModel())
                .build();
    }

    @Bean
    public QwenChatModel visionModel(DashScopeProperties props) {
        return QwenChatModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getVisionModel())
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(DashScopeProperties props) {
        return QwenEmbeddingModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getEmbeddingModel())
                .build();
    }

    @Bean
    public JadeMatchAssistant jadeMatchAssistant(QwenChatModel chatModel) {
        return AiServices.builder(JadeMatchAssistant.class)
                .chatModel(chatModel)
                .build();
    }
}
