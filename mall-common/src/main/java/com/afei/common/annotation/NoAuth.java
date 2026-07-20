package com.afei.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记无需登录即可访问的接口
 *
 * 打在方法上：该接口跳过 Token 校验（如登录、注册）
 * 打在类上：该 Controller 所有接口跳过 Token 校验
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAuth {
}
