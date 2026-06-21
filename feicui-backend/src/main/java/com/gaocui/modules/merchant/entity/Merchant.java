package com.gaocui.modules.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gaocui.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 商家实体 (邮箱验证码登录, 无密码).
 * tier: FREE 免费 / VIP 会员.
 */
@Getter
@Setter
@TableName("t_merchant")
public class Merchant extends BaseEntity {

    public static final String TIER_FREE = "FREE";
    public static final String TIER_VIP = "VIP";

    private String email;
    private String tier;
    /** VIP 到期时间(FREE 为 null) */
    private LocalDateTime vipExpireTime;
    /** 站内通知开关: 1 开 0 关 */
    private Integer webNotify;
    /** 邮件通知开关: 1 开 0 关 */
    private Integer emailNotify;
}
