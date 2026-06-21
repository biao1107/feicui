package com.gaocui.modules.merchant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改邮箱请求 (新邮箱 + 验证码).
 * PRD: 当前阶段校验可不做(随便改), 这里预留字段.
 */
@Getter
@Setter
public class UpdateEmailRequest {

    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
