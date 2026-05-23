package com.github.lianjiatech.retrofit.spring.boot.log;

import lombok.Data;

/**
 * 全局日志配置
 * @author 陈添明
 */
@Data
public class GlobalLogProperty {

    /**
     * 是否启用日志，默认关闭，避免开箱即用时产生非预期的日志开销与噪音；
     * 用户主动开启后即会按 {@link #logStrategy} 输出基础请求/响应行。
     */
    private boolean enable = false;

    /**
     * 日志名称，默认为{@link LoggingInterceptor} 的全类名
     */
    private String logName = LoggingInterceptor.class.getName();

    /**
     * 日志打印级别，支持的日志级别参见{@link LogLevel}
     * Log printing level, see {@link LogLevel} for supported log levels
     */
    private LogLevel logLevel = LogLevel.INFO;

    /**
     * 日志打印策略，支持的日志打印策略参见{@link LogStrategy}
     * Log printing strategy, see {@link LogStrategy} for supported log printing strategies
     * <p>
     * 默认 {@link LogStrategy#BASIC}，仅打印请求行/响应行（含状态码、耗时），开销可忽略；
     * 用户开启 {@link #enable} 后即可看到关键调用信息。
     */
    private LogStrategy logStrategy = LogStrategy.BASIC;

    /**
     * 是否聚合打印请求日志
     */
    private boolean aggregate = true;

    /**
     * 日志中需要隐藏的敏感请求头。
     * <p>
     * 默认遮蔽常见的鉴权与会话相关请求头，避免在 {@link LogStrategy#HEADERS}/{@link LogStrategy#BODY}
     * 级别下意外泄露凭证。OkHttp 的 {@code redactHeader} 是大小写不敏感匹配。
     * 用户在配置文件中设置该属性会整体覆盖默认值，因此自定义时需自行包含仍需遮蔽的项。
     */
    private String[] redactHeaders = {"Authorization", "Proxy-Authorization", "Cookie", "Set-Cookie"};
}
