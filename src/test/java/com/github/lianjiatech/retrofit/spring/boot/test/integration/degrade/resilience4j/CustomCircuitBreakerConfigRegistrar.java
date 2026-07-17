package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.resilience4j;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;
import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.CircuitBreakerConfigRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.CircuitBreakerConfigRegistry;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

/**
 * @author 陈添明
 * @since 2022/5/24 9:55 下午
 */
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {

    @Override
    public void register(CircuitBreakerConfigRegistry registry) {

        // 替换默认的CircuitBreakerConfig
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // 注册其它的CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(1)
                .minimumNumberOfCalls(1)
                .permittedNumberOfCallsInHalfOpenState(2)
                .build());

        registry.register("testCircuitBreakerConfig2", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(1)
                .minimumNumberOfCalls(1)
                .permittedNumberOfCallsInHalfOpenState(10)
                .build());
    }
}