package com.gaocui.common.api;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一 API 响应结构.
 * <pre>
 * { "code": 200, "message": "成功", "data": {...}, "timestamp": 1718000000000 }
 * </pre>
 */
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;
    private long timestamp;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ---- 成功 ----
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ---- 失败 ----
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
