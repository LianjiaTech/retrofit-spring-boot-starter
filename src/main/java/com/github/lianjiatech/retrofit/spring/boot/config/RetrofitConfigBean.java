package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseGlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceInstanceChooserInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import okhttp3.ConnectionPool;
import retrofit2.CallAdapter;
import retrofit2.Converter;

import java.util.Collection;
import java.util.Map;

/**
 * @author 陈添明
 */
public class RetrofitConfigBean {

    private final RetrofitProperties retrofitProperties;

    private Map<String, ConnectionPool> poolRegistry;

    private Collection<BaseGlobalInterceptor> globalInterceptors;

    private Collection<NetworkInterceptor> networkInterceptors;

    private BaseRetryInterceptor retryInterceptor;

    private ServiceInstanceChooserInterceptor serviceInstanceChooserInterceptor;

    private Class<? extends Converter.Factory>[] globalConverterFactoryClasses;

    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses;

    private BaseResourceNameParser resourceNameParser;

    public RetrofitProperties getRetrofitProperties() {
        return retrofitProperties;
    }


    public RetrofitConfigBean(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
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
