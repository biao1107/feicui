package com.gaocui.common.api;

/**
 * 全局业务状态码.
 * 约定: 2xx 成功, 4xx 客户端错误, 5xx 服务端/业务错误.
 */
public enum ResultCode {

    SUCCESS(200, "成功"),

    // ---- 通用客户端错误 ----
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),

    // ---- 通用服务端错误 ----
    SERVER_ERROR(500, "服务器开小差了"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // ---- 鉴权/验证码相关 1xxx ----
    VERIFY_CODE_SEND_FAIL(1001, "验证码发送失败"),
    VERIFY_CODE_INVALID(1002, "验证码无效或已过期"),
    VERIFY_CODE_ERROR(1003, "验证码错误"),
    LOGIN_FAIL(1004, "登录失败"),
    ACCOUNT_FROZEN(1005, "账号已被冻结"),

    // ---- 商家/会员相关 2xxx ----
    EMAIL_USED(2001, "邮箱已被注册"),
    MERCHANT_NOT_FOUND(2002, "商家不存在"),
    PRODUCT_LIMIT_REACHED(2003, "发布额度已用完，需升级 VIP"),
    VIP_EXPIRED(2004, "VIP 已过期"),

    // ---- 商品相关 3xxx ----
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_STATUS_INVALID(3002, "商品状态不允许此操作"),

    // ---- 客资相关 4xxx ----
    LEAD_NOT_FOUND(4001, "客资不存在"),
    LEAD_EMAIL_MASKED(4002, "该客资联系方式未解锁, 升级 VIP 后可标记"),

    // ---- 文件/AI 相关 5xxx ----
    FILE_UPLOAD_FAIL(5001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORT(5002, "不支持的文件类型"),
    AI_SERVICE_ERROR(5003, "AI 服务调用失败，请稍后重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
