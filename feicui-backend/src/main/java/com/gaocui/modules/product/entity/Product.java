package com.gaocui.modules.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.gaocui.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品实体.
 * status: DRAFT 草稿 / LISTED 已上架 / DELISTED 已下架.
 * tags / images 用 JSON 存储 (JacksonTypeHandler), 需 autoResultMap=true.
 */
@TableName(value = "t_product", autoResultMap = true)
public class Product extends BaseEntity {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_LISTED = "LISTED";
    public static final String STATUS_DELISTED = "DELISTED";

    private Long merchantId;
    private String title;
    private String brief;
    private String description;
    private BigDecimal price;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private String status;
    /** 文案是否 AI 生成: 0 否 1 是 */
    private Integer aiGenerated;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(Integer aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}
