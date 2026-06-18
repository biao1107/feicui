package com.gaocui.modules.ai.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品 - AI 文案生成结果(供商家编辑).
 */
public class AiGenerateResponse {

    private String title;
    private String brief;
    private String description;
    private BigDecimal price;
    private List<String> tags;

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
}
