package com.gaocui.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置 (前缀 oss).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    /** 访问域名前缀, 用于拼接图片可访问 URL */
    private String domain;
}
