package com.gaocui.modules.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * AI 找货匹配响应: 文案回复 + 翡翠卡片列表.
 */
@Getter
@Setter
public class AiMatchResponse {

    private String reply;
    private List<ProductCard> products;
}
