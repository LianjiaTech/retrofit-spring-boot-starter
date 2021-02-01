package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.LogLevel;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.LogStrategy;

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
     * 全局日志打印级别，支持的日志级别参见{@link LogLevel}
     * Log printing level, see {@link LogLevel} for supported log levels
     */
    private LogLevel globalLogLevel = LogLevel.INFO;

    /**
     * 全局日志打印策略，支持的日志打印策略参见{@link LogStrategy}
     * Log printing strategy, see {@link LogStrategy} for supported log printing strategies
     */
    private LogStrategy globalLogStrategy = LogStrategy.BASIC;


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

    public LogLevel getGlobalLogLevel() {
        return globalLogLevel;
    }

    public void setGlobalLogLevel(LogLevel globalLogLevel) {
        this.globalLogLevel = globalLogLevel;
    }

    public LogStrategy getGlobalLogStrategy() {
        return globalLogStrategy;
    }

    public void setGlobalLogStrategy(LogStrategy globalLogStrategy) {
        this.globalLogStrategy = globalLogStrategy;
    }
}
