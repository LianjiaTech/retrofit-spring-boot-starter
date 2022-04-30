package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.LogLevel;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.LogStrategy;

import lombok.Data;

/**
 * @author 陈添明
 */
@Data
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
}
