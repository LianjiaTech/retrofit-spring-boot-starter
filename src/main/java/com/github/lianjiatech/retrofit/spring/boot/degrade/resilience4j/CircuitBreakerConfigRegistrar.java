package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

/**
 * @author 陈添明
 * @since 2022/5/24 9:37 下午
 */
public interface CircuitBreakerConfigRegistrar {

    /**
     * 向#{@link CircuitBreakerConfigRegistry}注册数据
     * @param registry CircuitBreakerConfigRegistry
     */
    void register(CircuitBreakerConfigRegistry registry);
}
