package com.gaocui.modules.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情视图(商家自己的详情 + 游客详情共用).
 */
@Getter
@Setter
public class ProductDetailVO {

    private Long id;
    private Long merchantId;
    private String title;
    private String brief;
    private String description;
    private BigDecimal price;
    private List<String> tags;
    private List<String> images;
    private String status;
    private Integer aiGenerated;
    private LocalDateTime createdTime;
}
