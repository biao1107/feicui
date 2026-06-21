package com.gaocui.modules.lead.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 客资详情.
 * 字段: 留言时间 / 用户需求(留言) / 关联商品 / 用户邮箱 / 商家账号.
 * emailFullVisible: 本条买家邮箱是否完整可见(VIP 全部; 免费商家: 已联系全完整 + 未联系最近3条完整), 供前端决定是否允许复制.
 */
@Getter
@Setter
public class LeadDetailVO {

    private Long id;
    private Long productId;
    private String productTitle;
    private String message;
    private String buyerEmail;
    private String merchantEmail;
    private String status;
    private LocalDateTime createdTime;
    /** 本条买家邮箱是否完整可见: true=完整(VIP / 免费已联系 / 免费未联系最近3条), false=已脱敏 */
    private boolean emailFullVisible;
}
