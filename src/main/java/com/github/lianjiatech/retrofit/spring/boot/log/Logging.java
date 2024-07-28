package com.github.lianjiatech.retrofit.spring.boot.log;

import java.lang.annotation.*;

/**
 * @author 陈添明
 * @since 2022/4/30 9:47 下午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Logging {

    /**
     * 是否启用日志打印，针对当前接口或者方法
     *
     * @return 是否启用
     */
    boolean enable() default true;

    /**
     * 设置日志名称，效果相当于 {@code LoggerFactory.getLogger(logger)}。
     * <p>
     * 默认值为 {@link LoggingInterceptor} 的全类名。
     * <p>如果为空，使用默认值。</p>
     *
     * @return 日志名称
     */
    String logName() default "";

    /**
     * 日志打印级别，支持的日志级别参见{@link LogLevel}
     * 如果为NULL，则取全局日志打印级别
     * <p>
     * Log printing level, see {@link LogLevel} for supported log levels
     *
     * @return 日志打印级别
     */
    LogLevel logLevel() default LogLevel.INFO;

    /**
     * 日志打印策略，支持的日志打印策略参见{@link LogStrategy}
     * 如果为NULL，则取全局日志打印策略
     * <p>
     * Log printing strategy, see {@link LogStrategy} for supported log printing strategies
     *
     * @return 日志打印策略
     */
    LogStrategy logStrategy() default LogStrategy.BASIC;

    /**
     * 是否聚合打印请求日志
     * @return 是否聚合打印请求日志
     */
    boolean aggregate() default true;
}
