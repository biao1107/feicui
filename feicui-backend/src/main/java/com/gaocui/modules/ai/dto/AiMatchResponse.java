package com.gaocui.modules.ai.dto;

import java.util.List;

/**
 * AI 找货匹配响应: 文案回复 + 翡翠卡片列表.
 */
public class AiMatchResponse {

    private String reply;
    private List<ProductCard> products;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<ProductCard> getProducts() {
        return products;
    }

    public void setProducts(List<ProductCard> products) {
        this.products = products;
    }
}
