package com.github.lianjiatech.retrofit.spring.boot.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.core.AutoConfiguredRetrofitScannerRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.core.NoValidServiceInstanceChooser;
import com.github.lianjiatech.retrofit.spring.boot.core.PrototypeInterceptorBdfProcessor;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DefaultResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.degrade.ResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegradeInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalAndNetworkInterceptorFinder;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceInstanceChooserInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.util.ApplicationContextUtils;

import okhttp3.ConnectionPool;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author 陈添明
 */
@Configuration
@EnableConfigurationProperties(RetrofitProperties.class)
@AutoConfigureAfter({JacksonAutoConfiguration.class})
public class RetrofitAutoConfiguration implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RetrofitAutoConfiguration.class);

    private final RetrofitProperties retrofitProperties;

    private ApplicationContext applicationContext;

    public RetrofitAutoConfiguration(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }

    @Configuration
    public static class RetrofitProcessorAutoConfiguration {

        @Bean
        public static PrototypeInterceptorBdfProcessor prototypeInterceptorBdfProcessor() {
            return new PrototypeInterceptorBdfProcessor();
        }
    }

    @Bean
    public GlobalAndNetworkInterceptorFinder globalAndNetworkInterceptorFinder() {
        return new GlobalAndNetworkInterceptorFinder();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean(@Autowired(required = false) ResourceNameParser resourceNameParser,
            @Autowired(required = false) DegradeInterceptor degradeInterceptor)
            throws IllegalAccessException, InstantiationException {
        RetrofitConfigBean retrofitConfigBean =
                new RetrofitConfigBean(retrofitProperties, globalAndNetworkInterceptorFinder());
        // Initialize the connection pool
        Map<String, ConnectionPool> poolRegistry = new ConcurrentHashMap<>(4);
        Map<String, PoolConfig> pool = retrofitProperties.getPool();
        if (pool != null) {
            pool.forEach((poolName, poolConfig) -> {
                long keepAliveSecond = poolConfig.getKeepAliveSecond();
                int maxIdleConnections = poolConfig.getMaxIdleConnections();
                ConnectionPool connectionPool =
                        new ConnectionPool(maxIdleConnections, keepAliveSecond, TimeUnit.SECONDS);
                poolRegistry.put(poolName, connectionPool);
            });
        }
        retrofitConfigBean.setPoolRegistry(poolRegistry);

        // callAdapterFactory
        Class<? extends CallAdapter.Factory>[] globalCallAdapterFactories =
                retrofitProperties.getGlobalCallAdapterFactories();
        retrofitConfigBean.setGlobalCallAdapterFactoryClasses(globalCallAdapterFactories);

        // converterFactory
        Class<? extends Converter.Factory>[] globalConverterFactories =
                retrofitProperties.getGlobalConverterFactories();
        retrofitConfigBean.setGlobalConverterFactoryClasses(globalConverterFactories);

        // retryInterceptor
        RetryProperty retry = retrofitProperties.getRetry();
        Class<? extends BaseRetryInterceptor> retryInterceptor = retry.getRetryInterceptor();
        BaseRetryInterceptor retryInterceptorInstance =
                ApplicationContextUtils.getBeanOrNull(applicationContext, retryInterceptor);
        if (retryInterceptorInstance == null) {
            retryInterceptorInstance = retryInterceptor.newInstance();
        }
        BeanUtils.copyProperties(retry, retryInterceptorInstance);
        retrofitConfigBean.setRetryInterceptor(retryInterceptorInstance);

        // add ServiceInstanceChooserInterceptor
        ServiceInstanceChooser serviceInstanceChooser;
        try {
            serviceInstanceChooser = applicationContext.getBean(ServiceInstanceChooser.class);
        } catch (BeansException e) {
            serviceInstanceChooser = new NoValidServiceInstanceChooser();
        }

        ServiceInstanceChooserInterceptor serviceInstanceChooserInterceptor =
                new ServiceInstanceChooserInterceptor(serviceInstanceChooser);
        retrofitConfigBean.setServiceInstanceChooserInterceptor(serviceInstanceChooserInterceptor);

        retrofitConfigBean.setResourceNameParser(resourceNameParser);
        retrofitConfigBean.setDegradeInterceptor(degradeInterceptor);
        return retrofitConfigBean;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "retrofit.degrade.enable", havingValue = "true")
    public ResourceNameParser resourceNameParser() {
        return new DefaultResourceNameParser();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "retrofit.degrade.degrade-type", havingValue = "sentinel")
    @ConditionalOnBean(ResourceNameParser.class)
    public DegradeInterceptor degradeInterceptor(ResourceNameParser resourceNameParser) {
        return new SentinelDegradeInterceptor(resourceNameParser);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper jacksonObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    @ConditionalOnMissingBean
    @Autowired
    public JacksonConverterFactory jacksonConverterFactory(ObjectMapper objectMapper) {
        return JacksonConverterFactory.create(objectMapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Configuration
    @Import({AutoConfiguredRetrofitScannerRegistrar.class})
    @ConditionalOnMissingBean(RetrofitFactoryBean.class)
    public static class RetrofitScannerRegistrarNotFoundConfiguration implements InitializingBean {
        @Override
        public void afterPropertiesSet() {
            logger.debug("No {} found.", RetrofitFactoryBean.class.getName());
        }
    }

}
