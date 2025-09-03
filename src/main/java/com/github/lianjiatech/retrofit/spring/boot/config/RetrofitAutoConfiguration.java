package com.github.lianjiatech.retrofit.spring.boot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.core.AutoConfiguredRetrofitScannerRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.Constants;
import com.github.lianjiatech.retrofit.spring.boot.core.DefaultBaseUrlParser;
import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.core.PathMatchInterceptorBdfProcessor;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;
import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistry;
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
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryInterceptor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;

/**
 * @author 陈添明
 */
@AutoConfiguration
@EnableConfigurationProperties(RetrofitProperties.class)
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
    public BasicTypeConverterFactory basicTypeConverterFactory() {
        return BasicTypeConverterFactory.INSTANCE;
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
    public JacksonConverterFactory retrofitJacksonConverterFactory() {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return JacksonConverterFactory.create(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean(ServiceChooseInterceptor serviceChooseInterceptor,
            RetryInterceptor retryInterceptor, LoggingInterceptor loggingInterceptor,
            ErrorDecoderInterceptor errorDecoderInterceptor, SourceOkHttpClientRegistry sourceOkHttpClientRegistry) {

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
        return retrofitConfigBean;
    }

    @Configuration
    @Import({AutoConfiguredRetrofitScannerRegistrar.class})
    @ConditionalOnMissingBean(RetrofitFactoryBean.class)
    public static class RetrofitScannerRegistrarNotFoundConfiguration {
    }

    @Configuration
    @ConditionalOnClass(name = Constants.CIRCUIT_BREAKER_CLASS_NAME)
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

        @Bean
        @ConditionalOnMissingBean
        public RetrofitDegrade
        retrofitResilience4jRetrofitDegrade(CircuitBreakerConfigRegistry circuitBreakerConfigRegistry) {
            return new Resilience4jRetrofitDegrade(CircuitBreakerRegistry.ofDefaults(),
                    properties.getDegrade().getGlobalResilience4jDegrade(), circuitBreakerConfigRegistry);
        }
    }

    @ConditionalOnClass(name = Constants.SPH_U_CLASS_NAME)
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

}
