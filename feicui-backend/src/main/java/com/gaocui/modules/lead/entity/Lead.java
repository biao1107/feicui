package com.gaocui.modules.lead.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gaocui.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 客资实体 (买家留邮箱留言 → 进入商家客资).
 * status: PENDING 待联系 / CONTACTED 已联系.
 */
@Getter
@Setter
@TableName("t_lead")
public class Lead extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONTACTED = "CONTACTED";

    private Long productId;
    private Long merchantId;
    private String buyerEmail;
    private String message;
    private String status;
}
