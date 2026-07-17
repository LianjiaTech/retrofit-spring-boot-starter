package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;

/**
 * @author yukdawn@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Resilience4jDegrade {

    /**
     * 是否开启
     *
     * @return enable
     */
    boolean enable() default true;

    /**
     * 根据该名称从#{@link CircuitBreakerConfigRegistry}中获取CircuitBreakerConfig，作为当前接口或者方法的熔断配置
     *
     * @return circuitBreakerConfigName
     */
    String circuitBreakerConfigName() default Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG;
}