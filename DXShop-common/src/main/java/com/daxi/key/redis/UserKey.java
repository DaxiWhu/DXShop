package com.daxi.key.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块 Redis Key 定义
 */
@Getter
@AllArgsConstructor
public enum UserKey {
    /** 用户登录信息 Key：user:login:{token} */
    LOGIN("user:login:%s", "用户登录信息"),

    SHOP_LOGIN("shop:login:%s", "商家登录信息"),

    /** 短信验证码 Key：user:verify:code:{phone} */
    VERIFY_CODE("user:verify:code:%s", "短信验证码"),

    /** 验证码发送间隔限制 Key：user:verify:limit:{phone} */
    VERIFY_CODE_LIMIT("user:verify:limit:%s", "验证码发送间隔限制");

    private final String template;
    private final String desc;

    /**
     * 根据模板生成 Key
     */
    public String format(Object... args) {
        return String.format(this.template, args);
    }
}
