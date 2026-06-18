package com.gaocui.common.security;

/**
 * 当前登录用户(由 AuthInterceptor 解析 token 后写入, 请求结束清除).
 */
public record LoginUser(Long merchantId, String email, String tier) {
}
