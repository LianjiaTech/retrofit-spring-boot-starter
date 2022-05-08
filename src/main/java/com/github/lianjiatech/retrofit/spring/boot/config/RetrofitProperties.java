package com.github.lianjiatech.retrofit.spring.boot.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProperty;
import com.github.lianjiatech.retrofit.spring.boot.log.GlobalLogProperty;
import com.github.lianjiatech.retrofit.spring.boot.retry.GlobalRetryProperty;

import lombok.Data;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author 陈添明
 */
@ConfigurationProperties(prefix = "retrofit")
@Data
public class RetrofitProperties {

    private static final String DEFAULT_POOL = "default";

    /**
     * 连接池配置
     * Connection pool configuration
     */
    @NestedConfigurationProperty
    private Map<String, PoolConfig> pool = new LinkedHashMap<>();

    /**
     * 全局重试配置
     * retry config
     */
    @NestedConfigurationProperty
    private GlobalRetryProperty globalRetry = new GlobalRetryProperty();

    /**
     * 熔断降级配置
     * degrade config
     */
    @NestedConfigurationProperty
    private DegradeProperty degrade = new DegradeProperty();

    /**
     * 全局日志配置
     * log config
     */
    @NestedConfigurationProperty
    private GlobalLogProperty globalLog = new GlobalLogProperty();

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
    private Class<? extends Converter.Factory>[] globalConverterFactories = (Class<
            ? extends Converter.Factory>[])new Class[] {BasicTypeConverterFactory.class, JacksonConverterFactory.class};

    /**
     * 全局调用适配器工厂，转换器实例优先从Spring容器获取，如果没有获取到，则反射创建。
     * global call adapter factories, The  callAdapter instance is first obtained from the Spring container. If it is not obtained, it is created by reflection.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactories =
            (Class<? extends CallAdapter.Factory>[])new Class[] {BodyCallAdapterFactory.class,
                ResponseCallAdapterFactory.class};

    public Map<String, PoolConfig> getPool() {
        if (!pool.isEmpty()) {
            return pool;
        }
        pool.put(DEFAULT_POOL, new PoolConfig(5, 300));
        return pool;
    }
}
