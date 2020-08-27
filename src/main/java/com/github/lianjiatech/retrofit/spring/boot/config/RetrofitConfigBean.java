package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseGlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ErrorDecoderInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import okhttp3.ConnectionPool;
import retrofit2.CallAdapter;
import retrofit2.Converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 陈添明
 */
public class RetrofitConfigBean {

    private final RetrofitProperties retrofitProperties;

    private List<CallAdapter.Factory> callAdapterFactories;

    private List<Converter.Factory> converterFactories;

    private Map<String, ConnectionPool> poolRegistry;

    private Collection<BaseGlobalInterceptor> globalInterceptors;

    private Collection<NetworkInterceptor> networkInterceptors;

    private BaseRetryInterceptor retryInterceptor;

    private ErrorDecoderInterceptor errorDecoderInterceptor;

    public RetrofitProperties getRetrofitProperties() {
        return retrofitProperties;
    }


    public RetrofitConfigBean(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }


    public List<CallAdapter.Factory> getCallAdapterFactories() {
        return callAdapterFactories;
    }

    public void setCallAdapterFactories(List<CallAdapter.Factory> callAdapterFactories) {
        this.callAdapterFactories = callAdapterFactories;
    }

    public List<Converter.Factory> getConverterFactories() {
        return converterFactories;
    }

    public void setConverterFactories(List<Converter.Factory> converterFactories) {
        this.converterFactories = converterFactories;
    }

    public Map<String, ConnectionPool> getPoolRegistry() {
        return poolRegistry;
    }

    public void setPoolRegistry(Map<String, ConnectionPool> poolRegistry) {
        this.poolRegistry = poolRegistry;
    }

    public Collection<BaseGlobalInterceptor> getGlobalInterceptors() {
        return globalInterceptors;
    }

    public void setGlobalInterceptors(Collection<BaseGlobalInterceptor> globalInterceptors) {
        this.globalInterceptors = globalInterceptors;
    }

    public BaseRetryInterceptor getRetryInterceptor() {
        return retryInterceptor;
    }

    public void setRetryInterceptor(BaseRetryInterceptor retryInterceptor) {
        this.retryInterceptor = retryInterceptor;
    }

    public Collection<NetworkInterceptor> getNetworkInterceptors() {
        return networkInterceptors;
    }

    public void setNetworkInterceptors(Collection<NetworkInterceptor> networkInterceptors) {
        this.networkInterceptors = networkInterceptors;
    }

    public ErrorDecoderInterceptor getErrorDecoderInterceptor() {
        return errorDecoderInterceptor;
    }

    public void setErrorDecoderInterceptor(ErrorDecoderInterceptor errorDecoderInterceptor) {
        this.errorDecoderInterceptor = errorDecoderInterceptor;
    }
}
