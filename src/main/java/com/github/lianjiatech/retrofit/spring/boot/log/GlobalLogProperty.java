package com.github.lianjiatech.retrofit.spring.boot.log;

import lombok.Data;

/**
 * 全局日志配置
 * @author 陈添明
 */
@Data
public class GlobalLogProperty {

    /**
     * 是否启用
     */
    private boolean enable = true;

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
}
