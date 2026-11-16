package com.itheima.exception;

/**
 * AI 调用异常：code 用于区分超时、HTTP 状态、解析失败等类型，cause 保留原始异常链。
 */
public class AiException extends RuntimeException {

    private final String code;

    public AiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
