package com.gaocui.modules.merchant.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 商家个人中心资料.
 */
@Getter
@Setter
public class MerchantProfileVO {

    private Long id;
    private String email;
    private String tier;
    private LocalDateTime vipExpireTime;
    private Integer webNotify;
    private Integer emailNotify;
    /** 当前层级发布上限(2 或 100) */
    private Integer productLimit;
}
