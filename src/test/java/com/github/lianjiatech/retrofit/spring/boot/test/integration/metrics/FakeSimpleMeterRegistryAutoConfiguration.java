package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitAutoConfiguration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * 模拟 {@code spring-boot-starter-actuator} 通过 autoconfig 注册 {@link MeterRegistry} 的行为，
 * 供 {@link MetricsAutoConfigOrderingTest} 在标准 Spring Boot autoconfig 流程下验证 metrics 装配。
 *
 * <p><b>{@code @AutoConfiguration(after = RetrofitAutoConfiguration.class)}</b>：故意让本 fake
 * 排在 Retrofit 之后处理，构造 "MeterRegistry 比消费者后注册" 这个最不利场景。在该顺序下，
 * 任何依赖 {@code @ConditionalOnBean(MeterRegistry.class)} 求值时机的实现都会失败 ——
 * 当前 opt-in 方案直接通过依赖注入获取 {@code MeterRegistry}，由 Spring 在 lifecycle 阶段统一解析，
 * 不会受顺序影响。
 *
 * <p>不直接使用 {@code @TestConfiguration} 注册 {@code MeterRegistry}：
 * {@code @TestConfiguration} 会经由常规 @Configuration 解析路径处理，<b>在 autoconfig 流程之外</b>，
 * 无法真正模拟生产中 actuator 的装配链路。
 *
 * @author 陈添明
 */
@AutoConfiguration(after = RetrofitAutoConfiguration.class)
public class FakeSimpleMeterRegistryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
