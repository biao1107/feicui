package com.gaocui.modules.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 找货匹配返回的翡翠卡片(最多3张).
 */
@Getter
@Setter
public class ProductCard {

    private Long id;
    private String title;
    private List<String> tags;
    private BigDecimal price;
    /** 第一张图 */
    private String coverImage;
    /** 是否为 VIP 商家货源 */
    private boolean vip;
}
