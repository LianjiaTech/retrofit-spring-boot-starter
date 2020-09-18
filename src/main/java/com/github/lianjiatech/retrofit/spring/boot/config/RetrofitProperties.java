package com.github.lianjiatech.retrofit.spring.boot.config;


import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.DefaultRetryInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 陈添明
 */
@ConfigurationProperties(prefix = "retrofit")
public class RetrofitProperties {

    private static final String DEFAULT_POOL = "default";

    /**
     * 连接池配置
     * Connection pool configuration
     */
    private Map<String, PoolConfig> pool = new LinkedHashMap<>();

    /**
     * 启用 #{@link BodyCallAdapterFactory} 调用适配器
     * Enable #{@link BodyCallAdapterFactory} call adapter
     */
    private boolean enableBodyCallAdapter = true;

    /**
     * 启用 #{@link ResponseCallAdapterFactory} 调用适配器
     * Enable #{@link ResponseCallAdapterFactory} call adapter
     */
    private boolean enableResponseCallAdapter = true;

    /**
     * 启用日志打印
     * Enable log printing
     */
    private boolean enableLog = true;

    /**
     * 日志打印拦截器
     * Log print Interceptor
     */
    private Class<? extends BaseLoggingInterceptor> loggingInterceptor = DefaultLoggingInterceptor.class;


    /**
     * retry interceptor
     */
    private Class<? extends BaseRetryInterceptor> retryInterceptor = DefaultRetryInterceptor.class;


    /**
     * Disable Void return type
     */
    private boolean disableVoidReturnType = false;

    /**
     * 全局转换器工厂，转换器实例优先从Spring容器获取，如果没有获取到，则反射创建。
     * global converter factories, The converter instance is first obtained from the Spring container. If it is not obtained, it is created by reflection.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Converter.Factory>[] globalConverterFactories = (Class<? extends Converter.Factory>[]) new Class[]{JacksonConverterFactory.class};


    public Class<? extends BaseLoggingInterceptor> getLoggingInterceptor() {
        return loggingInterceptor;
    }

    public void setLoggingInterceptor(Class<? extends BaseLoggingInterceptor> loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    public Map<String, PoolConfig> getPool() {
        if (!pool.isEmpty()) {
            return pool;
        }
        pool.put(DEFAULT_POOL, new PoolConfig(5, 300));
        return pool;
    }

    public void setPool(Map<String, PoolConfig> pool) {
        this.pool = pool;
    }

    public boolean isEnableBodyCallAdapter() {
        return enableBodyCallAdapter;
    }

    public void setEnableBodyCallAdapter(boolean enableBodyCallAdapter) {
        this.enableBodyCallAdapter = enableBodyCallAdapter;
    }

    public boolean isEnableResponseCallAdapter() {
        return enableResponseCallAdapter;
    }

    public void setEnableResponseCallAdapter(boolean enableResponseCallAdapter) {
        this.enableResponseCallAdapter = enableResponseCallAdapter;
    }

    public boolean isEnableLog() {
        return enableLog;
    }

    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    public boolean isDisableVoidReturnType() {
        return disableVoidReturnType;
    }

    public void setDisableVoidReturnType(boolean disableVoidReturnType) {
        this.disableVoidReturnType = disableVoidReturnType;
    }

    public Class<? extends BaseRetryInterceptor> getRetryInterceptor() {
        return retryInterceptor;
    }

    public void setRetryInterceptor(Class<? extends BaseRetryInterceptor> retryInterceptor) {
        this.retryInterceptor = retryInterceptor;
    }

    public Class<? extends Converter.Factory>[] getGlobalConverterFactories() {
        return globalConverterFactories;
    }

    public void setGlobalConverterFactories(Class<? extends Converter.Factory>[] globalConverterFactories) {
        this.globalConverterFactories = globalConverterFactories;
    }
}
