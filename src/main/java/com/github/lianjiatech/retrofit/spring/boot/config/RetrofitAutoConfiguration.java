package com.github.lianjiatech.retrofit.spring.boot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseGlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseHttpExceptionMessageFormatter;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.HttpExceptionMessageFormatterInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import okhttp3.ConnectionPool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈添明
 */
@Configuration
@EnableConfigurationProperties(RetrofitProperties.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class RetrofitAutoConfiguration implements ApplicationContextAware {

    @Autowired
    private RetrofitProperties retrofitProperties;

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean(@Autowired ObjectMapper objectMapper) throws IllegalAccessException, InstantiationException {
        RetrofitConfigBean retrofitConfigBean = new RetrofitConfigBean(retrofitProperties);
        // 初始化连接池
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

        // 设置Http异常信息格式化器
        Class<? extends BaseHttpExceptionMessageFormatter> httpExceptionMessageFormatterClass = retrofitProperties.getHttpExceptionMessageFormatter();
        BaseHttpExceptionMessageFormatter alarmFormatter = httpExceptionMessageFormatterClass.newInstance();
        HttpExceptionMessageFormatterInterceptor httpExceptionMessageFormatterInterceptor = new HttpExceptionMessageFormatterInterceptor(alarmFormatter);
        retrofitConfigBean.setHttpExceptionMessageFormatterInterceptor(httpExceptionMessageFormatterInterceptor);

        // callAdapterFactory
        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        Collection<CallAdapter.Factory> callAdapterFactoryBeans = getBeans(CallAdapter.Factory.class);
        if (!CollectionUtils.isEmpty(callAdapterFactoryBeans)) {
            callAdapterFactories.addAll(callAdapterFactoryBeans);
        }
        if (retrofitProperties.isEnableBodyCallAdapter()) {
            callAdapterFactories.add(new BodyCallAdapterFactory());
        }
        if (retrofitProperties.isEnableResponseCallAdapter()) {
            callAdapterFactories.add(new ResponseCallAdapterFactory());
        }
        retrofitConfigBean.setCallAdapterFactories(callAdapterFactories);

        // converterFactory
        List<Converter.Factory> converterFactories = new ArrayList<>();
        Collection<Converter.Factory> converterFactoryBeans = getBeans(Converter.Factory.class);
        if (!CollectionUtils.isEmpty(converterFactoryBeans)) {
            converterFactories.addAll(converterFactoryBeans);
        }
        JacksonConverterFactory defaultJacksonConverterFactory = JacksonConverterFactory.create(objectMapper);
        converterFactories.add(defaultJacksonConverterFactory);
        retrofitConfigBean.setConverterFactories(converterFactories);
        // globalInterceptors
        Collection<BaseGlobalInterceptor> globalInterceptors = getBeans(BaseGlobalInterceptor.class);
        retrofitConfigBean.setGlobalInterceptors(globalInterceptors);

        // retryInterceptor
        Class<? extends BaseRetryInterceptor> retryInterceptor = retrofitProperties.getRetryInterceptor();
        retrofitConfigBean.setRetryInterceptor(retryInterceptor.newInstance());

        return retrofitConfigBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper jacksonObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private <U> Collection<U> getBeans(Class<U> clz) {
        try {
            Map<String, U> beanMap = applicationContext.getBeansOfType(clz);
            return beanMap.values();
        } catch (BeansException e) {
            // do nothing
        }
        return null;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
