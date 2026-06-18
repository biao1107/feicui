package com.gaocui.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 高翠网业务自定义配置 (前缀 gaocui).
 * 对应 application.yml 中 gaocui.* 配置项.
 */
@ConfigurationProperties(prefix = "gaocui")
public class GaocuiProperties {

    private Jwt jwt = new Jwt();
    private VerifyCode verifyCode = new VerifyCode();
    private Merchant merchant = new Merchant();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public VerifyCode getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(VerifyCode verifyCode) {
        this.verifyCode = verifyCode;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    /** JWT 鉴权配置 */
    public static class Jwt {
        private String secret;
        /** token 有效期(秒) */
        private long expire = 604800;
        private String header = "Authorization";
        private String tokenPrefix = "Bearer ";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getTokenPrefix() {
            return tokenPrefix;
        }

        public void setTokenPrefix(String tokenPrefix) {
            this.tokenPrefix = tokenPrefix;
        }
    }

    /** 验证码配置 */
    public static class VerifyCode {
        /** 有效期(秒) */
        private long expire = 300;
        /** 验证码长度 */
        private int length = 6;
        /** 开发模式: 控制台打印验证码, 不真实发邮件 */
        private boolean devPrint = true;

        public long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public boolean isDevPrint() {
            return devPrint;
        }

        public void setDevPrint(boolean devPrint) {
            this.devPrint = devPrint;
        }
    }

    /** 商家分层额度配置 */
    public static class Merchant {
        private int freeProductLimit = 2;
        private int vipProductLimit = 100;
        /** VIP 到期前 N 天提醒 */
        private int vipNoticeDays = 30;

        public int getFreeProductLimit() {
            return freeProductLimit;
        }

        public void setFreeProductLimit(int freeProductLimit) {
            this.freeProductLimit = freeProductLimit;
        }

        public int getVipProductLimit() {
            return vipProductLimit;
        }

        public void setVipProductLimit(int vipProductLimit) {
            this.vipProductLimit = vipProductLimit;
        }

        public int getVipNoticeDays() {
            return vipNoticeDays;
        }

        public void setVipNoticeDays(int vipNoticeDays) {
            this.vipNoticeDays = vipNoticeDays;
        }
    }
}
