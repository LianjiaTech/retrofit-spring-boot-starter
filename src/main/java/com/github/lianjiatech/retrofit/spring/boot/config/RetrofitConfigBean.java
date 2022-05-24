package com.github.lianjiatech.retrofit.spring.boot.config;

import java.util.Collections;
import java.util.List;

import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistry;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ErrorDecoderInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.NetworkInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ServiceChooseInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryInterceptor;

import lombok.Data;
import retrofit2.CallAdapter;
import retrofit2.Converter;

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
