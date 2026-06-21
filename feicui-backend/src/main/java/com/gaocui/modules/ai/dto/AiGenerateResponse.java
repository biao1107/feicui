package com.gaocui.modules.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品 - AI 文案生成结果(供商家编辑).
 */
@Getter
@Setter
public class AiGenerateResponse {

    private String title;
    private String brief;
    private String description;
    private BigDecimal price;
    private List<String> tags;
}
