package com.github.lianjiatech.retrofit.spring.boot.config;


import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import retrofit2.CallAdapter;
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
    @NestedConfigurationProperty
    private Map<String, PoolConfig> pool = new LinkedHashMap<>();

    /**
     * 重试配置
     * retry config
     */
    @NestedConfigurationProperty
    private RetryProperty retry = new RetryProperty();

    /**
     * 熔断降级配置
     * degrade config
     */
    @NestedConfigurationProperty
    private DegradeProperty degrade = new DegradeProperty();


    /**
     * 日志配置
     * log config
     */
    @NestedConfigurationProperty
    private LogProperty log = new LogProperty();


    /**
     * Disable Void return type
     */
    private boolean disableVoidReturnType = false;

    /**
     * 全局连接超时时间
     */
    private int globalConnectTimeoutMs = 10_000;

    /**
     * 全局读取超时时间
     */
    private int globalReadTimeoutMs = 10_000;

    /**
     * 全局写入超时时间
     */
    private int globalWriteTimeoutMs = 10_000;

    /**
     * 全局完整调用超时时间
     */
    private int globalCallTimeoutMs = 0;


    /**
     * 全局转换器工厂，转换器实例优先从Spring容器获取，如果没有获取到，则反射创建。
     * global converter factories, The converter instance is first obtained from the Spring container. If it is not obtained, it is created by reflection.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Converter.Factory>[] globalConverterFactories = (Class<? extends Converter.Factory>[]) new Class[]{BasicTypeConverterFactory.class, JacksonConverterFactory.class};

    /**
     * 全局调用适配器工厂，转换器实例优先从Spring容器获取，如果没有获取到，则反射创建。
     * global call adapter factories, The  callAdapter instance is first obtained from the Spring container. If it is not obtained, it is created by reflection.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactories = (Class<? extends CallAdapter.Factory>[]) new Class[]{BodyCallAdapterFactory.class, ResponseCallAdapterFactory.class};


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

    public boolean isDisableVoidReturnType() {
        return disableVoidReturnType;
    }

    public void setDisableVoidReturnType(boolean disableVoidReturnType) {
        this.disableVoidReturnType = disableVoidReturnType;
    }

    public Class<? extends Converter.Factory>[] getGlobalConverterFactories() {
        return globalConverterFactories;
    }

    public void setGlobalConverterFactories(Class<? extends Converter.Factory>[] globalConverterFactories) {
        this.globalConverterFactories = globalConverterFactories;
    }

    public Class<? extends CallAdapter.Factory>[] getGlobalCallAdapterFactories() {
        return globalCallAdapterFactories;
    }

    public void setGlobalCallAdapterFactories(Class<? extends CallAdapter.Factory>[] globalCallAdapterFactories) {
        this.globalCallAdapterFactories = globalCallAdapterFactories;
    }

    public RetryProperty getRetry() {
        return retry;
    }

    public void setRetry(RetryProperty retry) {
        this.retry = retry;
    }

    public DegradeProperty getDegrade() {
        return degrade;
    }

    public void setDegrade(DegradeProperty degrade) {
        this.degrade = degrade;
    }

    public LogProperty getLog() {
        return log;
    }

    public void setLog(LogProperty log) {
        this.log = log;
    }

    public int getGlobalConnectTimeoutMs() {
        return globalConnectTimeoutMs;
    }

    public void setGlobalConnectTimeoutMs(int globalConnectTimeoutMs) {
        this.globalConnectTimeoutMs = globalConnectTimeoutMs;
    }

    public int getGlobalReadTimeoutMs() {
        return globalReadTimeoutMs;
    }

    public void setGlobalReadTimeoutMs(int globalReadTimeoutMs) {
        this.globalReadTimeoutMs = globalReadTimeoutMs;
    }

    public int getGlobalWriteTimeoutMs() {
        return globalWriteTimeoutMs;
    }

    public void setGlobalWriteTimeoutMs(int globalWriteTimeoutMs) {
        this.globalWriteTimeoutMs = globalWriteTimeoutMs;
    }

    public int getGlobalCallTimeoutMs() {
        return globalCallTimeoutMs;
    }

    public void setGlobalCallTimeoutMs(int globalCallTimeoutMs) {
        this.globalCallTimeoutMs = globalCallTimeoutMs;
    }
}
