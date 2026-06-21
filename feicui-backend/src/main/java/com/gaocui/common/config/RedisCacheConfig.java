package com.gaocui.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis 缓存序列化配置.
 * 默认 JdkSerializationRedisSerializer 要求缓存对象 implements Serializable,
 * 改用 GenericJackson2JsonRedisSerializer(JSON):
 *   - 缓存对象(@Cacheable 返回值)无需 Serializable
 *   - 缓存内容 JSON 可读, 调试友好
 * 自定义 ObjectMapper: 注册 JavaTimeModule(支持 LocalDateTime) + default typing(保留类型信息供反序列化).
 */
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        // 保留类型信息(@class), 反序列化时按原类型还原; NON_FINAL 对非 final 类生效
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(om);
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));
    }
}
