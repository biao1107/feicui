package com.gaocui.modules.merchant.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 通知设置请求.
 * webNotify: 有人感兴趣时网页内通知(默认勾选, 不可取消).
 * emailNotify: 有人感兴趣时邮件通知(可取消).
 */
public class NotificationSettingRequest {

    @NotNull(message = "站内通知开关不能为空")
    private Integer webNotify;

    @NotNull(message = "邮件通知开关不能为空")
    private Integer emailNotify;

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
