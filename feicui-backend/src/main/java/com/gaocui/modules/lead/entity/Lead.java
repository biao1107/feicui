package com.gaocui.modules.lead.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gaocui.common.entity.BaseEntity;

/**
 * 客资实体 (买家留邮箱留言 → 进入商家客资).
 * status: PENDING 待联系 / CONTACTED 已联系.
 */
@TableName("t_lead")
public class Lead extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONTACTED = "CONTACTED";

    private Long productId;
    private Long merchantId;
    private String buyerEmail;
    private String message;
    private String status;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
