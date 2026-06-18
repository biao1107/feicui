package com.gaocui.modules.ai.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 找货匹配返回的翡翠卡片(最多3张).
 */
public class ProductCard {

    private Long id;
    private String title;
    private List<String> tags;
    private BigDecimal price;
    /** 第一张图 */
    private String coverImage;
    /** 是否为 VIP 商家货源 */
    private boolean vip;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }
}
