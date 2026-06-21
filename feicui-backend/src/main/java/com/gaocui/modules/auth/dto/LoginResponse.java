package com.gaocui.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 登录/注册成功响应.
 */
@Getter
@Setter
public class LoginResponse {

    private String token;
    private Long merchantId;
    private String email;
    private String tier;
    private LocalDateTime vipExpireTime;
}
