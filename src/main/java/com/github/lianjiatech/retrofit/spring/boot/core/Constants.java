package com.github.lianjiatech.retrofit.spring.boot.core;

/**
 * @author 陈添明
 * @since 2022/5/24 1:31 下午
 */
public interface Constants {

    String STR_EMPTY = "";

    String NO_SOURCE_OK_HTTP_CLIENT = "";

    String DEGRADE_TYPE = "retrofit.degrade.degrade-type";

    String RETROFIT = "retrofit";

    String DEFAULT_CIRCUIT_BREAKER_CONFIG = "defaultCircuitBreakerConfig";

    /**
     * 通用无效值标记（-1）。
     * <p>
     * 在 OkHttp 超时上下文中，-1 是非法值域（OkHttp 只接受 0 和正数），
     * 因此用作 {@code @Timeout} 等注解的"未配置，继承上层"默认值，
     * 不与合法超时值冲突。
     */
    int INVALID_VALUE = -1;
}
