package com.gaocui.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 高翠网业务自定义配置 (前缀 gaocui).
 * 对应 application.yml 中 gaocui.* 配置项.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "gaocui")
public class GaocuiProperties {

    private Jwt jwt = new Jwt();
    private VerifyCode verifyCode = new VerifyCode();
    private Merchant merchant = new Merchant();

    /** JWT 鉴权配置 */
    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        /** token 有效期(秒) */
        private long expire = 604800;
        private String header = "Authorization";
        private String tokenPrefix = "Bearer ";
    }

    /** 验证码配置 */
    @Getter
    @Setter
    public static class VerifyCode {
        /** 有效期(秒) */
        private long expire = 300;
        /** 验证码长度 */
        private int length = 6;
        /** 开发模式: 控制台打印验证码, 不真实发邮件 */
        private boolean devPrint = true;
    }

    /** 商家分层额度配置 */
    @Getter
    @Setter
    public static class Merchant {
        private int freeProductLimit = 2;
        private int vipProductLimit = 100;
        /** VIP 到期前 N 天提醒 */
        private int vipNoticeDays = 30;
    }
}
