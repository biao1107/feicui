package com.gaocui.modules.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 游客提交客资请求 (商品详情页"联系卖家").
 */
@Getter
@Setter
public class LeadSubmitRequest {

    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String buyerEmail;

    @NotBlank(message = "留言不能为空")
    @Size(max = 500, message = "留言最多500字")
    private String message;
}
