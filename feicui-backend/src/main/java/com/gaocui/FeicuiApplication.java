package com.gaocui;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 高翠网 - AI 翡翠匹配平台 后端启动类.
 *
 * <ul>
 *   <li>@MapperScan 扫描所有 modules 下的 Mapper 接口</li>
 *   <li>@ConfigurationPropertiesScan 启用自定义配置属性绑定 (@ConfigurationProperties)</li>
 *   <li>@EnableScheduling 开启定时任务 (VIP 到期检查等)</li>
 * </ul>
 */
@SpringBootApplication
@MapperScan("com.gaocui.modules.**.mapper")
@ConfigurationPropertiesScan("com.gaocui.common.config.properties")
@EnableScheduling
@EnableCaching
public class FeicuiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeicuiApplication.class, args);
        System.out.println("""

                ╔══════════════════════════════════════════╗
                ║   高翠网后端启动成功 (feicui-backend)        ║
                ║   接口文档: http://localhost:8080/api/doc.html ║
                ╚══════════════════════════════════════════╝""");
    }
}
