package com.github.lianjiatech.retrofit.spring.boot.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.degrade.ResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceInstanceChooserInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;

import lombok.Data;
import okhttp3.ConnectionPool;
import retrofit2.CallAdapter;
import retrofit2.Converter;

/**
 * @author 陈添明
 */
@Data
public class RetrofitConfigBean {

    private final RetrofitProperties retrofitProperties;

    private Map<String, ConnectionPool> poolRegistry;

    private List<GlobalInterceptor> globalInterceptors;

    private List<NetworkInterceptor> networkInterceptors;

    private BaseRetryInterceptor retryInterceptor;

    private ServiceInstanceChooserInterceptor serviceInstanceChooserInterceptor;

    private Class<? extends Converter.Factory>[] globalConverterFactoryClasses;

    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses;

    private ResourceNameParser resourceNameParser;

    private DegradeInterceptor degradeInterceptor;

    public RetrofitConfigBean(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }

    public List<GlobalInterceptor> getGlobalInterceptors() {
        if (globalInterceptors == null) {
            return Collections.emptyList();
        }
        return globalInterceptors;
    }

    public List<NetworkInterceptor> getNetworkInterceptors() {
        if (networkInterceptors == null) {
            return Collections.emptyList();
        }
        return networkInterceptors;
    }
}
