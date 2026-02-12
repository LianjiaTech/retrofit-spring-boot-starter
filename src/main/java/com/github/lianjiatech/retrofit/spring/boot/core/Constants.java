package com.github.lianjiatech.retrofit.spring.boot.core;

/**
 * @author 陈添明
 * @since 2022/5/24 1:31 下午
 */
public interface Constants {

    String STR_EMPTY = "";

    String NO_SOURCE_OK_HTTP_CLIENT = "";

    String SPH_U_CLASS_NAME = "com.alibaba.csp.sentinel.SphU";

    String DEGRADE_TYPE = "retrofit.degrade.degrade-type";

    String CIRCUIT_BREAKER_CLASS_NAME = "io.github.resilience4j.circuitbreaker.CircuitBreaker";

    String RETROFIT = "retrofit";

    String DEFAULT_CIRCUIT_BREAKER_CONFIG = "defaultCircuitBreakerConfig";

    int INVALID_VALUE = -1;
}
