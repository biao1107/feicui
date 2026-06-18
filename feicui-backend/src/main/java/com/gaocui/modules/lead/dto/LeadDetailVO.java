package com.gaocui.modules.lead.dto;

import java.time.LocalDateTime;

/**
 * 客资详情.
 * 字段: 留言时间 / 用户需求(留言) / 关联商品 / 用户邮箱 / 商家账号.
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
}
