package com.gaocui.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.gaocui.common.config.properties.OssProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 客户端 Bean.
 */
@Configuration
public class OssConfig {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties props) {
        return new OSSClientBuilder().build(
                props.getEndpoint(),
                props.getAccessKeyId(),
                props.getAccessKeySecret());
    }
}
