package com.gaocui.modules.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gaocui.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 商家实体 (邮箱验证码登录, 无密码).
 * tier: FREE 免费 / VIP 会员.
 */
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public LocalDateTime getVipExpireTime() {
        return vipExpireTime;
    }

    public void setVipExpireTime(LocalDateTime vipExpireTime) {
        this.vipExpireTime = vipExpireTime;
    }

    public Integer getWebNotify() {
        return webNotify;
    }

    public void setWebNotify(Integer webNotify) {
        this.webNotify = webNotify;
    }

    public Integer getEmailNotify() {
        return emailNotify;
    }

    public void setEmailNotify(Integer emailNotify) {
        this.emailNotify = emailNotify;
    }
}
