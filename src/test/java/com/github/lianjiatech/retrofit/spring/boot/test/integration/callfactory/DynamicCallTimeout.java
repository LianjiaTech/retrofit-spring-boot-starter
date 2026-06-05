package com.github.lianjiatech.retrofit.spring.boot.test.integration.callfactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 测试用方法级注解：指定动态 callTimeout 毫秒数。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicCallTimeout {

    /** callTimeout 毫秒数 */
    int ms();
}