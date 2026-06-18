
package com.daxi.result;

import lombok.Getter;

/**
 * 全局响应状态码枚举
 */
@Getter
public enum ResultCode {
    // 核心状态码
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录/登录过期"),
    FORBIDDEN(403, "无权限访问"),
    BUSINESS_ERROR(500, "业务异常"),
    SERVER_ERROR(505, "系统异常");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 判断是否为成功状态码
     */
    public boolean isSuccess() {
        return this.code == SUCCESS.code;
    }
}
