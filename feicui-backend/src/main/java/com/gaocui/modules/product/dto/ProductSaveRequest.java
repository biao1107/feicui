package com.gaocui.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品保存(创建/编辑)请求.
 * status 由单独接口控制, 这里仅承载可编辑字段.
 */
public class ProductSaveRequest {

    @NotBlank(message = "商品标题不能为空")
    private String title;
    private String brief;
    private String description;
    private BigDecimal price;
    private List<String> tags;
    private List<String> images;
    /** 文案是否 AI 生成 */
    private Integer aiGenerated;

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

    public Integer getAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(Integer aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}
