package com.daxi.ExceptionHandler;

import com.daxi.Exception.BusinessException;
import com.daxi.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // ===================== 1. 业务异常（你最常用） =====================
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        // 直接返回异常信息（图片超限、参数错误等）
        return Result.fail(e.getMessage());
    }

    // ===================== 2. 参数校验异常（@Valid校验失败） =====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        // 获取校验失败的提示信息
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return Result.fail("你发送的请求有异常：" + msg);
    }
    // 2. @RequestParam / @PathVariable 路径参数校验
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> validParamErr(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().iterator().next().getMessage();
        return Result.fail(msg);
    }

    // 3. 表单提交参数校验
    @ExceptionHandler(BindException.class)
    public Result<Void> validFormErr(BindException e) {
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return Result.fail(msg);
    }
    // ===================== 3. 空指针/系统异常 =====================
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        e.printStackTrace(); // 打印日志
        return Result.fail("服务器繁忙，请稍后重试");
    }

    // ===================== 4. 兜底：所有未知异常 =====================
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.fail("服务器繁忙，请稍后再试");
    }
}
