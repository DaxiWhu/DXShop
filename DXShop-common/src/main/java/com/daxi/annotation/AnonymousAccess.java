package com.daxi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // 可作用于方法或类上
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonymousAccess {
}
