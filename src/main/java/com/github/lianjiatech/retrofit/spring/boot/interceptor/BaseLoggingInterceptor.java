package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import org.slf4j.event.Level;

/**
 * @author 陈添明
 */
public abstract class BaseLoggingInterceptor implements NetworkInterceptor {

    /**
     * 日志打印级别
     * Log printing level
     */
    protected final Level logLevel;

    /**
     * 日志打印策略
     * Log printing strategy
     */
    protected final LogStrategy logStrategy;


    public BaseLoggingInterceptor(Level logLevel, LogStrategy logStrategy) {
        this.logLevel = logLevel;
        this.logStrategy = logStrategy;
    }
}
