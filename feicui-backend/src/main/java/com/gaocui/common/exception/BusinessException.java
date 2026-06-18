package com.gaocui.common.exception;

import com.gaocui.common.api.ResultCode;

/**
 * 业务异常. 抛出后由 GlobalExceptionHandler 统一捕获并转成标准 Result.
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
