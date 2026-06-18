package com.gaocui.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI3 / Knife4j 文档配置.
 * 访问: http://localhost:8080/api/doc.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("高翠网 - AI翡翠匹配平台 API")
                        .description("移动端 H5 · 买家AI找货 / 商家入驻卖货")
                        .version("1.0.0")
                        .contact(new Contact().name("高翠网")));
    }
}
