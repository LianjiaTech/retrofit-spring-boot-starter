package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * 测试上下文：注册一个 {@link SimpleMeterRegistry}，触发 metrics 自动配置。
 *
 * <p>使用 {@link TestConfiguration} 而非 {@code @Configuration}，避免该 Bean 通过 component scan
 * 泄漏到其它测试的 ApplicationContext，引起意料之外的 metrics 采集。
 *
 * @author 陈添明
 */
@TestConfiguration
public class MetricsTestConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
