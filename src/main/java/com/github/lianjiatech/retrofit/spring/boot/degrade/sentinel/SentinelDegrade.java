package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

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
     * 各降级策略对应的阈值。平均响应时间(ms)，异常比例(0-1)，异常数量(1-N)
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
