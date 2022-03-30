package com.github.lianjiatech.retrofit.spring.boot.config;

import java.util.List;
import java.util.Map;

import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalAndNetworkInterceptorFinder;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceInstanceChooserInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;

import okhttp3.ConnectionPool;
import retrofit2.CallAdapter;
import retrofit2.Converter;

/**
 * @author 陈添明
 */
public class RetrofitConfigBean {

    private final RetrofitProperties retrofitProperties;

    private Map<String, ConnectionPool> poolRegistry;

    private final List<GlobalInterceptor> globalInterceptors;

    private final List<NetworkInterceptor> networkInterceptors;

    private BaseRetryInterceptor retryInterceptor;

    private ServiceInstanceChooserInterceptor serviceInstanceChooserInterceptor;

    private Class<? extends Converter.Factory>[] globalConverterFactoryClasses;

    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses;

    private BaseResourceNameParser resourceNameParser;

    public RetrofitProperties getRetrofitProperties() {
        return retrofitProperties;
    }


    public RetrofitConfigBean(RetrofitProperties retrofitProperties,
            GlobalAndNetworkInterceptorFinder globalAndNetworkInterceptorFinder) {
        this.retrofitProperties = retrofitProperties;
        this.globalInterceptors = globalAndNetworkInterceptorFinder.getGlobalInterceptors();
        this.networkInterceptors = globalAndNetworkInterceptorFinder.getNetworkInterceptors();
    }


    public Map<String, ConnectionPool> getPoolRegistry() {
        return poolRegistry;
    }

    public void setPoolRegistry(Map<String, ConnectionPool> poolRegistry) {
        this.poolRegistry = poolRegistry;
    }

    public List<GlobalInterceptor> getGlobalInterceptors() {
        return globalInterceptors;
    }

    public BaseRetryInterceptor getRetryInterceptor() {
        return retryInterceptor;
    }

    public void setRetryInterceptor(BaseRetryInterceptor retryInterceptor) {
        this.retryInterceptor = retryInterceptor;
    }

    public List<NetworkInterceptor> getNetworkInterceptors() {
        return networkInterceptors;
    }

    public ServiceInstanceChooserInterceptor getServiceInstanceChooserInterceptor() {
        return serviceInstanceChooserInterceptor;
    }

    public void setServiceInstanceChooserInterceptor(ServiceInstanceChooserInterceptor serviceInstanceChooserInterceptor) {
        this.serviceInstanceChooserInterceptor = serviceInstanceChooserInterceptor;
    }

    public Class<? extends Converter.Factory>[] getGlobalConverterFactoryClasses() {
        return globalConverterFactoryClasses;
    }

    public void setGlobalConverterFactoryClasses(Class<? extends Converter.Factory>[] globalConverterFactoryClasses) {
        this.globalConverterFactoryClasses = globalConverterFactoryClasses;
    }

    public Class<? extends CallAdapter.Factory>[] getGlobalCallAdapterFactoryClasses() {
        return globalCallAdapterFactoryClasses;
    }

    public void setGlobalCallAdapterFactoryClasses(Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses) {
        this.globalCallAdapterFactoryClasses = globalCallAdapterFactoryClasses;
    }

    public BaseResourceNameParser getResourceNameParser() {
        return resourceNameParser;
    }

    public void setResourceNameParser(BaseResourceNameParser resourceNameParser) {
        this.resourceNameParser = resourceNameParser;
    }
}
