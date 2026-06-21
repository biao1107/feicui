package com.gaocui.modules.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI 调用专用线程池.
 * AI 匹配/生成是慢调用(数秒), 放独立池执行, 避免阻塞 Tomcat 工作线程导致整体不可用.
 * 隔离后: 慢 AI 调用最多占 max+queue 个任务位, 不会挤占其他接口的请求线程.
 */
@EnableAsync
@Configuration
public class AiAsyncConfig {

    @Bean("aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("ai-");
        // 队列满时由调用方线程执行(降级为同步), 不丢任务也不 OOM
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);
        ex.initialize();
        return ex;
    }
}
