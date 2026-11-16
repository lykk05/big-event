package com.itheima.exception;

import com.itheima.pojo.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

// 全局异常处理器
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 请求体字段校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return Result.error(400, message);
    }

    // 方法参数上的 @Pattern 等校验失败
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("；"));
        return Result.error(400, message);
    }

    // 请求体不是合法 JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败 traceId={}", MDC.get("traceId"), e);
        return Result.error(400, "请求体格式错误");
    }

    // 请求方法不支持
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不支持");
    }

    // 接口不存在
    @ExceptionHandler(NoResourceFoundException.class)
    public Result handleNotFound(NoResourceFoundException e) {
        return Result.error(404, "接口不存在");
    }

    // 业务异常：message 是给用户看的，可以安全回显
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    // AI 调用异常：对外给友好提示，异常链留在日志里
    @ExceptionHandler(AiException.class)
    public Result handleAiException(AiException e) {
        log.warn("AI 调用失败 code={} traceId={}", e.getCode(), MDC.get("traceId"), e);
        return Result.error(502, e.getMessage());
    }

    // 兜底：不回显内部细节
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("未预期异常 traceId={}", MDC.get("traceId"), e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
