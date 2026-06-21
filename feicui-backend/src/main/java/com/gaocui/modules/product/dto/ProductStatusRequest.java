package com.gaocui.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品上下架状态切换请求.
 * targetStatus: LISTED 上架 / DELISTED 下架.
 */
@Getter
@Setter
public class ProductStatusRequest {

    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;
}
