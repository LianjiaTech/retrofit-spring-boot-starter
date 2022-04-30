package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.log.LogLevel;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;

import lombok.Data;

/**
 * @author 陈添明
 */
@Data
public class LogProperty {

    /**
     * 是否启用全局日志打印
     * Enable log printing
     */
    private boolean enableGlobalLog = true;

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
}
