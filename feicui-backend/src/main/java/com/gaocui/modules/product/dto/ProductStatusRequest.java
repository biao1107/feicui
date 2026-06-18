package com.gaocui.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 商品上下架状态切换请求.
 * targetStatus: LISTED 上架 / DELISTED 下架.
 */
public class ProductStatusRequest {

    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }
}
