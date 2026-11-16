package com.itheima.exception;

/**
 * 业务异常：message 可以安全回显给前端，code 用于区分错误类型。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
