package com.github.lianjiatech.retrofit.plus.annotation;

import com.github.lianjiatech.retrofit.plus.interceptor.LogStrategy;
import org.slf4j.event.Level;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RetrofitClient {

    /**
     * 基础url, 支持占位符形式配置。<br>
     * 例如：http://${baseUrl.test}
     *
     * @return RetrofitClient的baseUrl
     */
    String baseUrl();

    /**
     * 使用的连接池名称<br>
     * default连接池自动加载，也可以手动配置覆盖默认default连接池属性
     *
     * @return 使用的连接池名称
     */
    String poolName() default "default";

    /**
     * 连接超时，单位为毫秒
     *
     * @return 连接超时时间
     */
    int connectTimeoutMs() default 10_000;

    /**
     * 读取超时，单位为毫秒
     *
     * @return 读取超时时间
     */
    int readTimeoutMs() default 10_000;

    /**
     * 写入超时，单位为毫秒
     *
     * @return 写入超时时间
     */
    int writeTimeoutMs() default 10_000;

    /**
     * 日志打印级别，支持的日志级别参见{@link Level}
     *
     * @return 日志打印级别
     */
    Level logLevel() default Level.INFO;

    /**
     * 日志打印策略，支持的日志打印策略参见{@link LogStrategy}
     *
     * @return 日志打印策略
     */
    LogStrategy logStrategy() default LogStrategy.BASIC;
}
