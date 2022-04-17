package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface SentinelDegrade {

    /**
     * RT模式下为慢调用临界 RT（超出该值计为慢调用）；异常比例/异常数模式下为对应的阈值
     */
    double count();

    /**
     * 熔断时长，单位为 s
     */
    int timeWindow() default 5;

    /**
     * 降级策略（0：平均响应时间；1：异常比例；2：异常数量）
     */
    int grade() default 0;
}
