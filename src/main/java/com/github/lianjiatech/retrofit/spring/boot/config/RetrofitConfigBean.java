package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistry;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ErrorDecoderInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceChooseInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryInterceptor;
import lombok.Data;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 陈添明
 */
@Data
public class RetrofitConfigBean {

    private final RetrofitProperties retrofitProperties;

    private List<GlobalInterceptor> globalInterceptors;

    private List<NetworkInterceptor> networkInterceptors;

    private RetryInterceptor retryInterceptor;

    private ServiceChooseInterceptor serviceChooseInterceptor;

    private Class<? extends Converter.Factory>[] globalConverterFactoryClasses;

    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses;

    private RetrofitDegrade retrofitDegrade;

    private LoggingInterceptor loggingInterceptor;

    private ErrorDecoderInterceptor errorDecoderInterceptor;

    private SourceOkHttpClientRegistry sourceOkHttpClientRegistry;

    /**
     * 基础 OkHttpClient。所有未指定 {@code sourceOkHttpClient} 的 {@code @RetrofitClient}
     * 都通过 {@link OkHttpClient#newBuilder()} 派生，以共享 connectionPool 与 dispatcher。
     */
    private OkHttpClient baseOkHttpClient;

    /**
     * 每个 Retrofit 接口对应的 baseUrl。生命周期与 ApplicationContext 绑定，避免在多 ClassLoader /
     * 多 Spring 上下文场景下 static 字段持有旧 Class 引用导致的内存泄漏。
     */
    private final ConcurrentMap<Class<?>, String> baseUrlMap = new ConcurrentHashMap<>();

    public RetrofitConfigBean(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }

    public void registerBaseUrl(Class<?> retrofitInterface, String baseUrl) {
        baseUrlMap.put(retrofitInterface, baseUrl);
    }

    public String getBaseUrl(Class<?> retrofitInterface) {
        return baseUrlMap.get(retrofitInterface);
    }
}
