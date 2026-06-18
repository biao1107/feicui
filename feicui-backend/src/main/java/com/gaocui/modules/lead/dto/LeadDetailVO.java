package com.gaocui.modules.lead.dto;

import java.time.LocalDateTime;

/**
 * 客资详情.
 * 字段: 留言时间 / 用户需求(留言) / 关联商品 / 用户邮箱 / 商家账号.
 * emailFullVisible: 本条买家邮箱是否完整可见(VIP 全部; 免费商家: 已联系全完整 + 未联系最近3条完整), 供前端决定是否允许复制.
 */
public class LeadDetailVO {

    private Long id;
    private Long productId;
    private String productTitle;
    private String message;
    private String buyerEmail;
    private String merchantEmail;
    private String status;
    private LocalDateTime createdTime;
    /** 本条买家邮箱是否完整可见: true=完整(VIP / 免费已联系 / 免费未联系最近3条), false=已脱敏 */
    private boolean emailFullVisible;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getMerchantEmail() {
        return merchantEmail;
    }

    public void setMerchantEmail(String merchantEmail) {
        this.merchantEmail = merchantEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isEmailFullVisible() {
        return emailFullVisible;
    }

    public void setEmailFullVisible(boolean emailFullVisible) {
        this.emailFullVisible = emailFullVisible;
    }
}
