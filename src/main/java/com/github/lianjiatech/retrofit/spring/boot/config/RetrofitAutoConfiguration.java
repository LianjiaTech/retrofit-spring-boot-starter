package com.github.lianjiatech.retrofit.spring.boot.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.core.AutoConfiguredRetrofitScannerRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.DefaultErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.core.NoValidServiceInstanceChooser;
import com.github.lianjiatech.retrofit.spring.boot.core.PathMatchInterceptorBdfProcessor;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DefaultResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.ResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ErrorDecoderInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceChooseInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryInterceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author 陈添明
 */
@Configuration
@EnableConfigurationProperties(RetrofitProperties.class)
@AutoConfigureAfter({JacksonAutoConfiguration.class})
@Slf4j
public class RetrofitAutoConfiguration {

    private final RetrofitProperties retrofitProperties;

    public RetrofitAutoConfiguration(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }

    @Configuration
    public static class RetrofitProcessorAutoConfiguration {

        @Bean
        public static PathMatchInterceptorBdfProcessor prototypeInterceptorBdfProcessor() {
            return new PathMatchInterceptorBdfProcessor();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean(@Autowired(required = false) ResourceNameParser resourceNameParser,
            @Autowired(required = false) RetrofitDegrade retrofitDegrade,
            @Autowired(required = false) List<GlobalInterceptor> globalInterceptors,
            @Autowired(required = false) List<NetworkInterceptor> networkInterceptors,
            ServiceChooseInterceptor serviceChooseInterceptor, RetryInterceptor retryInterceptor,
            LoggingInterceptor loggingInterceptor, ErrorDecoderInterceptor errorDecoderInterceptor) {

        RetrofitConfigBean retrofitConfigBean = new RetrofitConfigBean(retrofitProperties);
        retrofitConfigBean.setGlobalInterceptors(globalInterceptors);
        retrofitConfigBean.setNetworkInterceptors(networkInterceptors);
        retrofitConfigBean.setResourceNameParser(resourceNameParser);
        retrofitConfigBean.setRetrofitDegrade(retrofitDegrade);
        retrofitConfigBean.setServiceChooseInterceptor(serviceChooseInterceptor);
        retrofitConfigBean.setRetryInterceptor(retryInterceptor);
        retrofitConfigBean.setLoggingInterceptor(loggingInterceptor);
        retrofitConfigBean.setErrorDecoderInterceptor(errorDecoderInterceptor);

        Map<String, ConnectionPool> poolRegistry = new HashMap<>(4);
        retrofitProperties.getPool().forEach((poolName, poolConfig) -> {
            long keepAliveSecond = poolConfig.getKeepAliveSecond();
            int maxIdleConnections = poolConfig.getMaxIdleConnections();
            ConnectionPool connectionPool =
                    new ConnectionPool(maxIdleConnections, keepAliveSecond, TimeUnit.SECONDS);
            poolRegistry.put(poolName, connectionPool);
        });
        retrofitConfigBean.setPoolRegistry(poolRegistry);
        retrofitConfigBean.setGlobalCallAdapterFactoryClasses(retrofitProperties.getGlobalCallAdapterFactories());
        retrofitConfigBean.setGlobalConverterFactoryClasses(retrofitProperties.getGlobalConverterFactories());
        return retrofitConfigBean;
    }

    @Bean
    public BodyCallAdapterFactory bodyCallAdapterFactory() {
        return new BodyCallAdapterFactory();
    }

    @Bean
    public ResponseCallAdapterFactory responseCallAdapterFactory() {
        return new ResponseCallAdapterFactory();
    }

    @Bean
    public BasicTypeConverterFactory basicTypeConverterFactory() {
        return new BasicTypeConverterFactory();
    }

    @Bean
    public DefaultErrorDecoder defaultErrorDecoder() {
        return new DefaultErrorDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoderInterceptor errorDecoderInterceptor() {
        return new ErrorDecoderInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryInterceptor retryInterceptor() {
        return new RetryInterceptor(retrofitProperties.getRetry());
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor logInterceptor() {
        return new LoggingInterceptor(retrofitProperties.getLog());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceInstanceChooser serviceInstanceChooser() {
        return new NoValidServiceInstanceChooser();
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceChooseInterceptor serviceChooseInterceptor(@Autowired ServiceInstanceChooser serviceInstanceChooser) {
        return new ServiceChooseInterceptor(serviceInstanceChooser);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "retrofit.degrade.enable", havingValue = "true")
    public ResourceNameParser resourceNameParser() {
        return new DefaultResourceNameParser();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "retrofit.degrade.degrade-type", havingValue = RetrofitDegrade.SENTINEL)
    @ConditionalOnBean(ResourceNameParser.class)
    public RetrofitDegrade retrofitDegrade(ResourceNameParser resourceNameParser) {
        return new SentinelRetrofitDegrade(resourceNameParser);
    }

    @Bean
    @ConditionalOnMissingBean
    public JacksonConverterFactory jacksonConverterFactory() {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return JacksonConverterFactory.create(objectMapper);
    }

    @Configuration
    @Import({AutoConfiguredRetrofitScannerRegistrar.class})
    @ConditionalOnMissingBean(RetrofitFactoryBean.class)
    public static class RetrofitScannerRegistrarNotFoundConfiguration {}

}
