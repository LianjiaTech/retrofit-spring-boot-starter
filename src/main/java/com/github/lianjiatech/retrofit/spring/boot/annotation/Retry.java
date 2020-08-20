package com.github.lianjiatech.retrofit.spring.boot.annotation;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Retry {

    /**
     * 最大重试次数，最大可设置为10
     *
     * @return 最大重试次数
     */
    int maxRetries() default 3;

    /**
     * 重试时间间隔
     *
     * @return 重试时间间隔
     */
    int intervalMs() default 100;
}
