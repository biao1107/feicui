package com.gaocui.modules.lead.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 客资列表项.
 * buyerEmail: VIP 全部完整; 免费商家: 已联系全完整 + 未联系最近3条完整, 其余脱敏.
 */
@Getter
@Setter
public class LeadListItemVO {

    private Long id;
    private Long productId;
    private String productTitle;
    private String buyerEmail;
    private String message;
    private String status;
    private LocalDateTime createdTime;
}
