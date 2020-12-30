package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor;

/**
 * @author 陈添明
 */
public class LogProperty {

    /**
     * 启用日志打印
     * Enable log printing
     */
    private boolean enable = true;

    /**
     * 日志打印拦截器
     * Log print Interceptor
     */
    private Class<? extends BaseLoggingInterceptor> loggingInterceptor = DefaultLoggingInterceptor.class;


    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Class<? extends BaseLoggingInterceptor> getLoggingInterceptor() {
        return loggingInterceptor;
    }

    public void setLoggingInterceptor(Class<? extends BaseLoggingInterceptor> loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }
}
