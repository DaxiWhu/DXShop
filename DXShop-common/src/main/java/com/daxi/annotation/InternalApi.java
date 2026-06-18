package com.daxi.annotation;

import java.lang.annotation.*;

/**
 * 内部接口注解
 * <p>
 * 标记该接口只能被内部服务调用（通过 Feign 或网关转发），
 * 不允许外部直接访问。
 * </p>
 * <p>
 * 使用该注解的方法需要具备内部调用密钥或特定的请求头才能访问。
 * </p>
 *
 * @author Daxi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {
    
    /**
     * 是否需要验证内部密钥
     * <p>
     * 默认为 true，表示需要验证内部调用密钥
     * </p>
     *
     * @return 是否需要验证
     */
    boolean requireSecret() default true;
    
    /**
     * 允许的服务列表
     * <p>
     * 如果指定了服务列表，则只有这些服务可以调用该接口
     * 为空表示不限制调用来源
     * </p>
     *
     * @return 允许调用的服务名称列表
     */
    String[] allowedServices() default {};
}
