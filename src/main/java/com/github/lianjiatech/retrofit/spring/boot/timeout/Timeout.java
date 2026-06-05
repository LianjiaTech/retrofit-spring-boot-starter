package com.github.lianjiatech.retrofit.spring.boot.timeout;

import java.lang.annotation.*;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;

/**
 * 方法级 / 类级超时配置，优先级高于全局超时属性。
 * <p>
 * 优先级链：方法 {@code @Timeout} → 类 {@code @Timeout} → 全局 {@code GlobalTimeoutProperty}
 * <p>
 * 属性默认值 {@link Constants#INVALID_VALUE} (-1) 表示"未配置，继承上层优先级链"；
 * 设为 0 表示"无超时"；设为正数表示具体超时毫秒数。
 * <p>
 * -1 是 OkHttp 超时的非法值域（OkHttp 只接受 0 和正数），因此用作"未配置"标记不会与合法超时值冲突。
 * <p>
 * Method-level / class-level timeout configuration, higher priority than global timeout properties.
 * <p>
 * Priority chain: method {@code @Timeout} → class {@code @Timeout} → global {@code GlobalTimeoutProperty}
 * <p>
 * Default value {@link Constants#INVALID_VALUE} (-1) means "not configured, inherit from upper level";
 * 0 means "no timeout"; positive value means timeout in milliseconds.
 * <p>
 * -1 is outside OkHttp's valid timeout range (OkHttp accepts only 0 and positive values),
 * so it safely serves as the "not configured" marker without conflicting with legal timeout values.
 *
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Timeout {

    /**
     * 连接超时（毫秒）。默认 {@link Constants#INVALID_VALUE} 表示继承上层。
     * <p>
     * Connect timeout in milliseconds. Default {@link Constants#INVALID_VALUE} means inherit from upper level.
     */
    int connectTimeoutMs() default Constants.INVALID_VALUE;

    /**
     * 读取超时（毫秒）。默认 {@link Constants#INVALID_VALUE} 表示继承上层。
     * <p>
     * Read timeout in milliseconds. Default {@link Constants#INVALID_VALUE} means inherit from upper level.
     */
    int readTimeoutMs() default Constants.INVALID_VALUE;

    /**
     * 写入超时（毫秒）。默认 {@link Constants#INVALID_VALUE} 表示继承上层。
     * <p>
     * Write timeout in milliseconds. Default {@link Constants#INVALID_VALUE} means inherit from upper level.
     */
    int writeTimeoutMs() default Constants.INVALID_VALUE;

    /**
     * 完整调用超时（毫秒）。默认 {@link Constants#INVALID_VALUE} 表示继承上层。
     * <p>
     * Call timeout in milliseconds. Default {@link Constants#INVALID_VALUE} means inherit from upper level.
     */
    int callTimeoutMs() default Constants.INVALID_VALUE;
}