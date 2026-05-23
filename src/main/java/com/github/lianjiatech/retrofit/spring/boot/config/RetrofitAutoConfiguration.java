package com.github.lianjiatech.retrofit.spring.boot.config;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.alibaba.csp.sentinel.SphU;
import com.github.lianjiatech.retrofit.spring.boot.core.*;
import com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.CircuitBreakerConfigRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.CircuitBreakerConfigRegistry;
import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.Resilience4jRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ErrorDecoderInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceChooseInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.metrics.DefaultRetrofitTagsProvider;
import com.github.lianjiatech.retrofit.spring.boot.metrics.MetricsInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.metrics.MetricsProperty;
import com.github.lianjiatech.retrofit.spring.boot.metrics.RetrofitTagsProvider;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryInterceptor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author 陈添明
 */
@AutoConfiguration
@EnableConfigurationProperties(RetrofitProperties.class)
@Slf4j
public class RetrofitAutoConfiguration {

    private final RetrofitProperties retrofitProperties;

    @Autowired(required = false)
    private List<SourceOkHttpClientRegistrar> sourceOkHttpClientRegistrars;
    @Autowired(required = false)
    private RetrofitDegrade retrofitDegrade;
    @Autowired(required = false)
    private List<GlobalInterceptor> globalInterceptors;
    @Autowired(required = false)
    private List<NetworkInterceptor> networkInterceptors;
    /**
     * 由 {@link MetricsConfiguration} 在引入 Micrometer 时贡献，否则保持 null。Bean 名固定为
     * {@code retrofitMetricsInterceptor}，避免与用户自定义的 Interceptor Bean 冲突。
     */
    @Autowired(required = false)
    @Qualifier("retrofitMetricsInterceptor")
    private Interceptor retrofitMetricsInterceptor;

    public RetrofitAutoConfiguration(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }

    @Configuration
    public static class RetrofitAdvanceConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "retrofit", name = "auto-set-prototype-scope-for-path-math-interceptor",
                matchIfMissing = true)
        public static PathMatchInterceptorBdfProcessor pathMatchInterceptorBdfProcessor() {
            return new PathMatchInterceptorBdfProcessor();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SourceOkHttpClientRegistry sourceOkHttpClientRegistry() {
        return new SourceOkHttpClientRegistry(sourceOkHttpClientRegistrars);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder.DefaultErrorDecoder retrofitDefaultErrorDecoder() {
        return new ErrorDecoder.DefaultErrorDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoderInterceptor retrofitErrorDecoderInterceptor() {
        return new ErrorDecoderInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryInterceptor retrofitRetryInterceptor() {
        return new RetryInterceptor(retrofitProperties.getGlobalRetry());
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultBaseUrlParser defaultBaseUrlParser() {
        return new DefaultBaseUrlParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor retrofitLoggingInterceptor() {
        return new LoggingInterceptor(retrofitProperties.getGlobalLog());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceInstanceChooser retrofitServiceInstanceChooser() {
        return new ServiceInstanceChooser.NoValidServiceInstanceChooser();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceChooseInterceptor
    retrofitServiceChooseInterceptor(ServiceInstanceChooser serviceInstanceChooser) {
        return new ServiceChooseInterceptor(serviceInstanceChooser);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public JacksonConverterFactory retrofitJacksonConverterFactory(
            @Autowired(required = false) com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return objectMapper != null ? JacksonConverterFactory.create(objectMapper) : JacksonConverterFactory.create();
    }

    @Configuration
    @ConditionalOnClass(name = "tools.jackson.databind.ObjectMapper")
    public static class Jackson3Configuration {

        @Bean
        @ConditionalOnMissingBean
        public Jackson3ConverterFactory retrofitJackson3ConverterFactory(
                @Autowired(required = false) tools.jackson.databind.ObjectMapper objectMapper) {
            return objectMapper != null ? Jackson3ConverterFactory.create(objectMapper) : Jackson3ConverterFactory.create();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean(ServiceChooseInterceptor serviceChooseInterceptor,
            RetryInterceptor retryInterceptor, LoggingInterceptor loggingInterceptor,
            ErrorDecoderInterceptor errorDecoderInterceptor, SourceOkHttpClientRegistry sourceOkHttpClientRegistry,
            @Qualifier("retrofitBaseOkHttpClient") OkHttpClient baseOkHttpClient) {

        RetrofitConfigBean retrofitConfigBean = new RetrofitConfigBean(retrofitProperties);
        retrofitConfigBean.setGlobalInterceptors(globalInterceptors);
        retrofitConfigBean.setNetworkInterceptors(networkInterceptors);
        retrofitConfigBean.setRetrofitDegrade(retrofitDegrade);
        retrofitConfigBean.setServiceChooseInterceptor(serviceChooseInterceptor);
        retrofitConfigBean.setRetryInterceptor(retryInterceptor);
        retrofitConfigBean.setLoggingInterceptor(loggingInterceptor);
        retrofitConfigBean.setErrorDecoderInterceptor(errorDecoderInterceptor);
        retrofitConfigBean.setGlobalCallAdapterFactoryClasses(retrofitProperties.getGlobalCallAdapterFactories());
        retrofitConfigBean.setGlobalConverterFactoryClasses(retrofitProperties.getGlobalConverterFactories());
        retrofitConfigBean.setSourceOkHttpClientRegistry(sourceOkHttpClientRegistry);
        retrofitConfigBean.setBaseOkHttpClient(baseOkHttpClient);
        retrofitConfigBean.setMetricsInterceptor(retrofitMetricsInterceptor);
        return retrofitConfigBean;
    }

    /**
     * 共享的基础 {@link OkHttpClient}。所有未通过 {@code sourceOkHttpClient} 指定自定义客户端的
     * {@code @RetrofitClient} 都会基于它派生，复用同一份 connectionPool 与 dispatcher，避免每个接口
     * 独立持有线程池/连接池造成的资源浪费。
     * <p>
     * destroyMethod 留空，由 {@link OkHttpClientLifecycle} 显式负责关停，避免 Spring 误调用。
     */
    @Bean(name = "retrofitBaseOkHttpClient", destroyMethod = "")
    @ConditionalOnMissingBean(name = "retrofitBaseOkHttpClient")
    public OkHttpClient retrofitBaseOkHttpClient() {
        GlobalTimeoutProperty t = retrofitProperties.getGlobalTimeout();
        GlobalConnectionPoolProperty p = retrofitProperties.getGlobalConnectionPool();
        return new OkHttpClient.Builder()
                .connectTimeout(t.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(t.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(t.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
                .callTimeout(t.getCallTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(p.getMaxIdleConnections(),
                        p.getKeepAliveDurationMs(), TimeUnit.MILLISECONDS))
                .build();
    }

    @Bean
    public OkHttpClientLifecycle retrofitOkHttpClientLifecycle(
            @Qualifier("retrofitBaseOkHttpClient") OkHttpClient baseOkHttpClient) {
        return new OkHttpClientLifecycle(baseOkHttpClient);
    }

    /**
     * 在 ApplicationContext 关闭时停止 dispatcher 线程池、回收 connectionPool 中的空闲连接、
     * 关闭 cache。所有通过 {@link OkHttpClient#newBuilder()} 派生的子 client 都共享这些资源，
     * 因此只需对根 client 执行一次。
     */
    static class OkHttpClientLifecycle implements DisposableBean {

        private final OkHttpClient baseClient;

        OkHttpClientLifecycle(OkHttpClient baseClient) {
            this.baseClient = baseClient;
        }

        @Override
        public void destroy() {
            try {
                baseClient.dispatcher().executorService().shutdown();
            } catch (Exception e) {
                log.warn("Failed to shutdown OkHttp dispatcher executor", e);
            }
            try {
                baseClient.connectionPool().evictAll();
            } catch (Exception e) {
                log.warn("Failed to evict OkHttp connection pool", e);
            }
            if (baseClient.cache() != null) {
                try {
                    baseClient.cache().close();
                } catch (IOException e) {
                    log.warn("Failed to close OkHttp cache", e);
                }
            }
        }
    }

    @Configuration
    @Import({AutoConfiguredRetrofitScannerRegistrar.class})
    @ConditionalOnMissingBean(RetrofitFactoryBean.class)
    public static class RetrofitScannerRegistrarNotFoundConfiguration {
    }

    @Configuration
    @ConditionalOnClass(CircuitBreaker.class)
    @ConditionalOnProperty(name = Constants.DEGRADE_TYPE, havingValue = RetrofitDegrade.RESILIENCE4J)
    @EnableConfigurationProperties(RetrofitProperties.class)
    public static class Resilience4jConfiguration {

        private final RetrofitProperties properties;

        @Autowired(required = false)
        private List<CircuitBreakerConfigRegistrar> circuitBreakerConfigRegistrars;

        public Resilience4jConfiguration(RetrofitProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        public CircuitBreakerConfigRegistry retrofitCircuitBreakerConfigRegistry() {
            return new CircuitBreakerConfigRegistry(circuitBreakerConfigRegistrars);
        }

        /**
         * 默认 CircuitBreakerRegistry。若用户已在 Spring 容器中注册了自定义 CircuitBreakerRegistry（如通过
         * resilience4j-spring-boot 自动配置），则 @ConditionalOnMissingBean 保证不会覆盖它。
         */
        @Bean
        @ConditionalOnMissingBean
        public CircuitBreakerRegistry retrofitCircuitBreakerRegistry() {
            return CircuitBreakerRegistry.ofDefaults();
        }

        @Bean
        @ConditionalOnMissingBean
        public RetrofitDegrade retrofitResilience4jRetrofitDegrade(CircuitBreakerRegistry circuitBreakerRegistry,
                CircuitBreakerConfigRegistry circuitBreakerConfigRegistry) {
            return new Resilience4jRetrofitDegrade(circuitBreakerRegistry,
                    properties.getDegrade().getGlobalResilience4jDegrade(), circuitBreakerConfigRegistry);
        }
    }

    @Configuration
    @ConditionalOnClass(SphU.class)
    @ConditionalOnProperty(name = Constants.DEGRADE_TYPE, havingValue = RetrofitDegrade.SENTINEL)
    @EnableConfigurationProperties(RetrofitProperties.class)
    public static class SentinelConfiguration {

        private final RetrofitProperties properties;

        public SentinelConfiguration(RetrofitProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        public RetrofitDegrade retrofitSentinelRetrofitDegrade() {
            return new SentinelRetrofitDegrade(properties.getDegrade().getGlobalSentinelDegrade());
        }
    }

    /**
     * Micrometer 指标采集自动配置。
     *
     * <p><b>启用条件</b>：
     * <ul>
     *     <li>类路径存在 {@link MeterRegistry}（即用户引入了 Micrometer 或相关 starter）；</li>
     *     <li>{@code retrofit.metrics.enable=true}（<b>必须显式开启</b>，默认关闭）。</li>
     * </ul>
     *
     * <p><b>为什么是 opt-in 而非根据 {@code @ConditionalOnBean(MeterRegistry.class)} 自动开启</b>：
     * autoconfig 加载顺序由 jar 扫描决定，{@code @ConditionalOnBean} 在 {@code REGISTER_BEAN} 阶段
     * 求值，只要 Retrofit 这个 autoconfig 排在 actuator 的 {@code SimpleMetricsExportAutoConfiguration}
     * 之前，{@code MeterRegistry} 的 BeanDefinition 还没声明，条件就会误判为"无 MeterRegistry"，
     * 整个 metrics 模块被错误跳过。让用户显式开启则把控制权交给用户，行为可预测：
     * <ul>
     *     <li>用户引入 actuator 不会"被自动埋点"，避免无意中产生指标；</li>
     *     <li>用户显式开启时若容器内并没有 {@code MeterRegistry}，Spring 会在依赖注入时报错，
     *         明确告知"你启用了 metrics 但没有 MeterRegistry"，而不是悄悄什么都没采集。</li>
     * </ul>
     */
    @Configuration
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "retrofit.metrics", name = "enable", havingValue = "true")
    @EnableConfigurationProperties(RetrofitProperties.class)
    public static class MetricsConfiguration {

        private final RetrofitProperties properties;

        public MetricsConfiguration(RetrofitProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        public RetrofitTagsProvider retrofitTagsProvider() {
            return new DefaultRetrofitTagsProvider(properties.getMetrics());
        }

        /**
         * Bean 名固定为 {@code retrofitMetricsInterceptor}，与 {@link RetrofitAutoConfiguration} 中的
         * {@code @Qualifier} 一致。返回 {@link Interceptor} 接口类型，避免在用户禁用 metrics 时仍然
         * 加载 {@link MetricsInterceptor}。
         *
         * <p>{@code MeterRegistry} 直接作为方法参数注入：用户开启 metrics 但容器内不存在
         * {@code MeterRegistry} 时 Spring 会抛 {@code NoSuchBeanDefinitionException}，
         * 明确暴露配置错误，而不是静默无指标。
         */
        @Bean(name = "retrofitMetricsInterceptor")
        @ConditionalOnMissingBean(name = "retrofitMetricsInterceptor")
        public Interceptor retrofitMetricsInterceptor(MeterRegistry meterRegistry,
                RetrofitTagsProvider tagsProvider) {
            return new MetricsInterceptor(meterRegistry, tagsProvider, properties.getMetrics());
        }
    }

}
