package com.gaocui.modules.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品管理列表项(每行: 图/标题/价格/发布时间/状态).
 */
@Getter
@Setter
public class ProductListItemVO {

    private Long id;
    /** 第一张图 */
    private String coverImage;
    private String title;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdTime;
}
