package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface SentinelDegrade {

    /**
     * 是否开启
     */
    boolean enable() default true;

    /**
     * 各降级策略对应的阈值。平均响应时间(ms)，异常比例(0-1)，异常数量(1-N)
     */
    double count() default 1000;

    /**
     * 熔断时长，单位为 s
     */
    int timeWindow() default 5;

    /**
     * 降级策略（0：平均响应时间；1：异常比例；2：异常数量）
     */
    int grade() default 0;
}
