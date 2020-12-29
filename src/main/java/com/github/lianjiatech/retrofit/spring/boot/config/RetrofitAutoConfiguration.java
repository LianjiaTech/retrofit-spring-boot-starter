package com.github.lianjiatech.retrofit.spring.boot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.core.*;
import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegradeRuleInitializer;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseGlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceInstanceChooserInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.util.ApplicationContextUtils;
import okhttp3.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈添明
 */
@Configuration
@EnableConfigurationProperties(RetrofitProperties.class)
@AutoConfigureAfter({JacksonAutoConfiguration.class})
public class RetrofitAutoConfiguration implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RetrofitAutoConfiguration.class);

    @Autowired
    private RetrofitProperties retrofitProperties;

    private ApplicationContext applicationContext;

    @Configuration
    public static class RetrofitProcessorAutoConfiguration {

        @Bean
        public static PrototypeInterceptorBdfProcessor prototypeInterceptorBdfProcessor() {
            return new PrototypeInterceptorBdfProcessor();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean() throws IllegalAccessException, InstantiationException {
        RetrofitConfigBean retrofitConfigBean = new RetrofitConfigBean(retrofitProperties);
        // Initialize the connection pool
        Map<String, ConnectionPool> poolRegistry = new ConcurrentHashMap<>(4);
        Map<String, PoolConfig> pool = retrofitProperties.getPool();
        if (pool != null) {
            pool.forEach((poolName, poolConfig) -> {
                long keepAliveSecond = poolConfig.getKeepAliveSecond();
                int maxIdleConnections = poolConfig.getMaxIdleConnections();
                ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveSecond, TimeUnit.SECONDS);
                poolRegistry.put(poolName, connectionPool);
            });
        }
        retrofitConfigBean.setPoolRegistry(poolRegistry);

        // callAdapterFactory
        Class<? extends CallAdapter.Factory>[] globalCallAdapterFactories = retrofitProperties.getGlobalCallAdapterFactories();
        retrofitConfigBean.setGlobalCallAdapterFactoryClasses(globalCallAdapterFactories);

        // converterFactory
        Class<? extends Converter.Factory>[] globalConverterFactories = retrofitProperties.getGlobalConverterFactories();
        retrofitConfigBean.setGlobalConverterFactoryClasses(globalConverterFactories);

        // globalInterceptors
        Collection<BaseGlobalInterceptor> globalInterceptors = ApplicationContextUtils.getBeans(applicationContext, BaseGlobalInterceptor.class);
        retrofitConfigBean.setGlobalInterceptors(globalInterceptors);

        // retryInterceptor
        RetryProperty retry = retrofitProperties.getRetry();
        Class<? extends BaseRetryInterceptor> retryInterceptor = retry.getRetryInterceptor();
        BaseRetryInterceptor retryInterceptorInstance = retryInterceptor.newInstance();
        BeanUtils.copyProperties(retry, retryInterceptorInstance);
        retrofitConfigBean.setRetryInterceptor(retryInterceptorInstance);

        // add networkInterceptor
        Collection<NetworkInterceptor> networkInterceptors = ApplicationContextUtils.getBeans(applicationContext, NetworkInterceptor.class);
        retrofitConfigBean.setNetworkInterceptors(networkInterceptors);

        // add ServiceInstanceChooserInterceptor
        ServiceInstanceChooser serviceInstanceChooser;
        try {
            serviceInstanceChooser = applicationContext.getBean(ServiceInstanceChooser.class);
        } catch (BeansException e) {
            serviceInstanceChooser = new NoValidServiceInstanceChooser();
        }

        ServiceInstanceChooserInterceptor serviceInstanceChooserInterceptor = new ServiceInstanceChooserInterceptor(serviceInstanceChooser);
        retrofitConfigBean.setServiceInstanceChooserInterceptor(serviceInstanceChooserInterceptor);

        // resource name parser
        DegradeProperty degrade = retrofitProperties.getDegrade();
        Class<? extends BaseResourceNameParser> resourceNameParser = degrade.getResourceNameParser();
        retrofitConfigBean.setResourceNameParser(resourceNameParser.newInstance());

        return retrofitConfigBean;
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

    @Bean
    public RetrofitDegradeRuleInitializer retrofitDegradeRuleInitializer() {
        return new RetrofitDegradeRuleInitializer(retrofitProperties.getDegrade());
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
