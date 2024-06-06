package com.github.lianjiatech.retrofit.spring.boot.log;

import lombok.Data;

/**
 * 全局日志配置
 * @author 陈添明
 */
@Data
public class GlobalLogProperty {

    /**
     * 是否启用日志
     */
    private boolean enable = true;

    /**
     * logger 名字，默认为{@link LoggingInterceptor} 的全类名
     */
    private String logger = LoggingInterceptor.class.getName();

    /**
     * 日志打印级别，支持的日志级别参见{@link LogLevel}
     * Log printing level, see {@link LogLevel} for supported log levels
     */
    private LogLevel logLevel = LogLevel.INFO;

    /**
     * 日志打印策略，支持的日志打印策略参见{@link LogStrategy}
     * Log printing strategy, see {@link LogStrategy} for supported log printing strategies
     */
    private LogStrategy logStrategy = LogStrategy.BASIC;

    /**
     * 是否聚合打印请求日志
     */
    private boolean aggregate = true;
}
