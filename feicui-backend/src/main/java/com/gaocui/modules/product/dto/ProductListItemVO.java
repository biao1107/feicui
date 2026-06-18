package com.gaocui.modules.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品管理列表项(每行: 图/标题/价格/发布时间/状态).
 */
public class ProductListItemVO {

    private Long id;
    /** 第一张图 */
    private String coverImage;
    private String title;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
