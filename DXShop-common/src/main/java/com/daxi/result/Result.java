package com.daxi.result;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局统一返回结果
 * 不可变设计：仅通过静态工厂方法创建，构造后不可修改
 */
@Getter
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    private final String msg;

    private final T data;

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + (data != null ? data: "null") +
                '}';
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 判断是否成功（由 code 推导，避免冗余字段导致数据不一致）
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    // ==================== 成功工厂方法 ====================

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    // ==================== 失败工厂方法 ====================

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.BUSINESS_ERROR.getCode(), ResultCode.BUSINESS_ERROR.getMsg(), null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.BUSINESS_ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> Result<T> fail(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }
}