package com.daxi.response;

public class UserResponse {
    public static final String MAX_USER_NAME_ERROR="用户名的长度不能超过20";
    public static final String MAX_SKU_DSC_LENGTH_ERROR="商品描述的长度不能超过100";
    public static final String MAX_COMMENT_LENGTH_ERROR="评论的长度不能超过1000";
    public static final String MAX_COMMENT_IMAGE_LENGTH_ERROR="评论图片的数量不能超过20";
    public static final String NOT_LOGIN="未登录";
    public static final String RECEIVER_NAME_ERROR="收货人姓名不能为空且长度不能超过50";
    public static final String PHONE_ERROR="手机号不能为空且长度不能超过20";
    public static final String ADDRESS_ERROR="地址信息不能为空且长度不能超过100";
    public static final String PHONE_NOT_REGISTERED = "该手机号未注册";
    public static final String PASSWORD_ERROR = "手机号或密码错误";
    public static final String ACCOUNT_FROZEN = "账号已被冻结";
    public static final String ACCOUNT_CANCELLED = "账号已注销";
    public static final String LOGIN_SUCCESS = "登录成功";

    /** 注册相关 */
    public static final String PHONE_ALREADY_REGISTERED = "该手机号已注册";
    public static final String REGISTER_SUCCESS = "注册成功";

    /** 验证码相关 */
    public static final String VERIFY_CODE_SEND_SUCCESS = "验证码发送成功";
    public static final String VERIFY_CODE_SEND_TOO_FREQUENT = "验证码发送过于频繁，请稍后再试";
    public static final String VERIFY_CODE_EXPIRED = "验证码已过期，请重新获取";
    public static final String VERIFY_CODE_ERROR = "验证码错误";

}
