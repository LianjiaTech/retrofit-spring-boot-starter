package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;

import lombok.Data;

/**
 * 全局Resilience4j降级配置
 * @author 陈添明
 * @since 2022/5/8 10:46 上午
 */
@Data
public class GlobalResilience4jDegradeProperty {

    /**
     * 是否开启
     */
    private boolean enable = false;

    /***
     * 根据该名称从#{@link CircuitBreakerConfigRegistry}获取CircuitBreakerConfig，作为全局熔断配置
     */
    private String circuitBreakerConfigName = Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG;
}
