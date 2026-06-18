package com.gaocui.modules.merchant.dto;

import java.time.LocalDateTime;

/**
 * 商家个人中心资料.
 */
public class MerchantProfileVO {

    private Long id;
    private String email;
    private String tier;
    private LocalDateTime vipExpireTime;
    private Integer webNotify;
    private Integer emailNotify;
    /** 当前层级发布上限(2 或 100) */
    private Integer productLimit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public LocalDateTime getVipExpireTime() {
        return vipExpireTime;
    }

    public void setVipExpireTime(LocalDateTime vipExpireTime) {
        this.vipExpireTime = vipExpireTime;
    }

    public Integer getWebNotify() {
        return webNotify;
    }

    public void setWebNotify(Integer webNotify) {
        this.webNotify = webNotify;
    }

    public Integer getEmailNotify() {
        return emailNotify;
    }

    public void setEmailNotify(Integer emailNotify) {
        this.emailNotify = emailNotify;
    }

    public Integer getProductLimit() {
        return productLimit;
    }

    public void setProductLimit(Integer productLimit) {
        this.productLimit = productLimit;
    }
}
