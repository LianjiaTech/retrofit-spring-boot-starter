package com.github.lianjiatech.retrofit.spring.boot.annotation;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseLoggingInterceptor;
import org.slf4j.event.Level;
import retrofit2.Retrofit;

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
     * When calling {@link Retrofit#create(Class)} on the resulting {@link Retrofit} instance, eagerly validate the
     * configuration of all methods in the supplied interface.
     *
     * @return validateEagerly
     */
    boolean validateEagerly() default false;

    /**
     * 使用的连接池名称<br>
     * default连接池自动加载，也可以手动配置覆盖默认default连接池属性
     *
     * @return 使用的连接池名称
     */
    String poolName() default "default";

    /**
     * Sets the default connect timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds.
     *
     * @return 连接超时时间
     */
    int connectTimeoutMs() default 10_000;

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds.
     *
     * @return 读取超时时间
     */
    int readTimeoutMs() default 10_000;

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds
     *
     * @return 写入超时时间
     */
    int writeTimeoutMs() default 10_000;


    /**
     * Sets the default timeout for complete calls. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds.
     *
     * @return callTimeout
     */
    int callTimeoutMs() default 0;

    /**
     * Sets the interval between HTTP/2 and web socket pings initiated by this client.
     * Use this to automatically send ping frames until either the connection fails or it is closed.
     * This keeps the connection alive and may detect connectivity failures.
     *
     * @return pingInterval
     */
    int pingIntervalMs() default 0;


    /**
     * Configure this client to allow protocol redirects from HTTPS to HTTP and from HTTP to HTTPS.
     * Redirects are still first restricted by followRedirects. Defaults to true.
     *
     * @return followSslRedirects
     */
    boolean followSslRedirects() default true;

    /**
     * Configure this client to follow redirects. If unset, redirects will be followed.
     *
     * @return followRedirects
     */
    boolean followRedirects() default true;

    /**
     * Configure this client to retry or not when a connectivity problem is encountered.
     * By default, this client silently recovers from the following problems:
     *
     * @return retryOnConnectionFailure
     */
    boolean retryOnConnectionFailure() default true;


    /**
     * 针对当前接口是否启用日志打印
     *
     * @return
     */
    boolean enableLog() default true;

    /**
     * 日志打印级别，支持的日志级别参见{@link Level}
     *
     * @return 日志打印级别
     */
    Level logLevel() default Level.INFO;

    /**
     * 日志打印策略，支持的日志打印策略参见{@link BaseLoggingInterceptor.LogStrategy}
     *
     * @return 日志打印策略
     */
    BaseLoggingInterceptor.LogStrategy logStrategy() default BaseLoggingInterceptor.LogStrategy.BASIC;
}
