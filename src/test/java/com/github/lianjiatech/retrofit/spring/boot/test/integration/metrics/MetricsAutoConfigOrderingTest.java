package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitAutoConfiguration;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.metrics.RetrofitTagsProvider;

import io.micrometer.core.instrument.MeterRegistry;
import okhttp3.Interceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 metrics 装配在 Spring Boot autoconfig 加载流程中的行为：
 * <ul>
 *     <li>默认关闭：用户没设置 {@code retrofit.metrics.enable=true} 时整套 metrics autoconfig 跳过；</li>
 *     <li>显式开启：即使 {@link MeterRegistry} 由<i>排在 Retrofit 之后</i>的 autoconfig 注册，
 *         metrics 拦截器仍能正确装配（因为 {@code @ConditionalOnProperty} 配 + 直接依赖注入，
 *         本身不依赖 {@code @ConditionalOnBean} 的求值时机）；</li>
 *     <li>显式开启但容器内没有 {@code MeterRegistry}：启动直接失败，把配置错误暴露出来，
 *         而不是悄悄不采集指标。</li>
 * </ul>
 *
 * <p>本测试通过 {@link FakeSimpleMeterRegistryAutoConfiguration} 模拟 actuator 通过 autoconfig
 * 注册 MeterRegistry 的真实路径，并显式声明 {@code after = RetrofitAutoConfiguration.class}
 * 制造"消费者先于生产者注册"这个曾经会失败的场景。
 *
 * @author 陈添明
 */
public class MetricsAutoConfigOrderingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RetrofitAutoConfiguration.class,
                    FakeSimpleMeterRegistryAutoConfiguration.class));

    @Test
    public void metricsDisabledByDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(MeterRegistry.class);
            // 默认 retrofit.metrics.enable 缺省即关闭：本组件什么都不注册
            assertThat(ctx).doesNotHaveBean("retrofitMetricsInterceptor");
            assertThat(ctx).doesNotHaveBean(RetrofitTagsProvider.class);

            RetrofitConfigBean cfg = ctx.getBean(RetrofitConfigBean.class);
            assertThat(cfg.getMetricsInterceptor())
                    .as("metrics interceptor must be null when not explicitly enabled")
                    .isNull();
        });
    }

    @Test
    public void metricsEnabledExplicitlyEvenIfMeterRegistryComesFromLaterAutoconfig() {
        runner.withPropertyValues("retrofit.metrics.enable=true").run(ctx -> {
            assertThat(ctx).hasSingleBean(MeterRegistry.class);
            assertThat(ctx).hasBean("retrofitMetricsInterceptor");
            assertThat(ctx).hasSingleBean(RetrofitTagsProvider.class);

            RetrofitConfigBean cfg = ctx.getBean(RetrofitConfigBean.class);
            Interceptor interceptor = cfg.getMetricsInterceptor();
            assertThat(interceptor)
                    .as("RetrofitConfigBean must hold a non-null metrics interceptor when explicitly enabled")
                    .isNotNull()
                    .isSameAs(ctx.getBean("retrofitMetricsInterceptor", Interceptor.class));
        });
    }

    @Test
    public void enablingMetricsWithoutMeterRegistryFailsFast() {
        // 用户开启了 metrics 但容器内没有 MeterRegistry：应当启动失败，
        // 把"配置错误"显式抛出来，而不是悄无声息地不采集指标
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RetrofitAutoConfiguration.class))
                .withPropertyValues("retrofit.metrics.enable=true")
                .run(ctx -> assertThat(ctx).hasFailed()
                        .getFailure()
                        .hasMessageContaining("MeterRegistry"));
    }
}
