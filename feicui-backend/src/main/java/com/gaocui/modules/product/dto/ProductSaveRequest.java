package com.gaocui.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品保存(创建/编辑)请求.
 * status 由单独接口控制, 这里仅承载可编辑字段.
 */
@Getter
@Setter
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
}
