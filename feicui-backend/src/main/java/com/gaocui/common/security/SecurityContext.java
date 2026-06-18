package com.gaocui.common.security;

/**
 * 基于 ThreadLocal 的登录上下文. Controller/Service 可直接获取当前商家.
 */
public final class SecurityContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long currentMerchantId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.merchantId();
    }

    public static boolean isVip() {
        LoginUser u = HOLDER.get();
        return u != null && "VIP".equalsIgnoreCase(u.tier());
    }

    /** 请求结束必须调用, 防止线程复用导致数据串号 */
    public static void clear() {
        HOLDER.remove();
    }
}
