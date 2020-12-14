package com.github.lianjiatech.retrofit.spring.boot.annotation;

import com.github.lianjiatech.retrofit.spring.boot.core.DefaultErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.LogStrategy;
import org.slf4j.event.Level;
import retrofit2.CallAdapter;
import retrofit2.Converter;
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
     * 绝对URL（协议是必需的）。
     * 可以指定为属性键，例如：$ {propertyKey}。
     * 如果baseUrl没有配置，则必须配置serviceId以及，path可选配置。
     *
     * An absolute URL (the protocol is necessary).
     * Can be specified as property key, eg: ${propertyKey}.
     * If baseUrl is not configured, you must configure serviceId and path optional configuration.
     *
     *
     * @return baseUrl
     */
    String baseUrl() default "";

    /**
     * The name of the service.
     * Can be specified as property key, eg: ${propertyKey}.
     */
    String serviceId() default "";

    /**
     * Path prefix to be used by all method-level mappings.
     */
    String path() default "";

    /**
     * 适用于当前接口的转换器工厂，优先级比全局转换器工厂更高。转换器实例优先从Spring容器获取，如果没有获取到，则反射创建。
     * Converter factory for the current interface, higher priority than global converter factory.
     * The converter instance is first obtained from the Spring container. If it is not obtained, it is created by reflection.
     */
    Class<? extends Converter.Factory>[] converterFactories() default {};

    /**
     * 适用于当前接口的调用适配器工厂，优先级比全局调用适配器工厂更高。转换器实例优先从Spring容器获取，如果没有获取到，则反射创建。
     * callAdapter factory for the current interface, higher priority than global callAdapter factory.
     * The converter instance is first obtained from the Spring container. If it is not obtained, it is created by reflection.
     */
    Class<? extends CallAdapter.Factory>[] callAdapterFactories() default {};

    /**
     * Fallback class for the specified retrofit client interface. The fallback class must
     * implement the interface annotated by this annotation and be a valid spring bean.
     */
    Class<?> fallback() default void.class;


    /**
     * Define a fallback factory for the specified Feign client interface. The fallback
     * factory must produce instances of fallback classes that implement the interface
     * annotated by {@link RetrofitClient}.The fallback factory must be a valid spring bean.
     * bean.
     */
    Class<?> fallbackFactory() default void.class;


    /**
     * 针对当前接口是否启用日志打印
     * Whether to enable log printing for the current interface
     *
     * @return
     */
    boolean enableLog() default true;

    /**
     * 日志打印级别，支持的日志级别参见{@link Level}
     * Log printing level, see {@link Level} for supported log levels
     *
     * @return logLevel
     */
    Level logLevel() default Level.INFO;

    /**
     * 日志打印策略，支持的日志打印策略参见{@link LogStrategy}
     * Log printing strategy, see {@link LogStrategy} for supported log printing strategies
     *
     * @return logStrategy
     */
    LogStrategy logStrategy() default LogStrategy.BASIC;


    /**
     * When calling {@link Retrofit#create(Class)} on the resulting {@link Retrofit} instance, eagerly validate the
     * configuration of all methods in the supplied interface.
     *
     * @return validateEagerly
     */
    boolean validateEagerly() default false;

    /**
     * 当前接口采用的错误解码器，当请求发生异常或者收到无效响应结果的时候，将HTTP相关信息解码到异常中，无效响应由业务自己判断。
     * 一般情况下，每个服务对应的无效响应各不相同，可以自定义对应的{@link ErrorDecoder}，然后配置在这里。
     * <p>
     * The error decoder used in the current interface will decode HTTP related information into the exception when an exception occurs in the request or an invalid response result is received.
     * The invalid response is determined by the business itself.
     * In general, the invalid response corresponding to each service is different, you can customize the corresponding {@link ErrorDecoder}, and then configure it here.
     *
     * @return ErrorDecoder
     */
    Class<? extends ErrorDecoder> errorDecoder() default DefaultErrorDecoder.class;

    /**
     * connection pool name
     *
     * @return connection pool name
     */
    String poolName() default "default";

    /**
     * Sets the default connect timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds.
     *
     * @return connectTimeoutMs
     */
    int connectTimeoutMs() default 10_000;

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds.
     *
     * @return readTimeoutMs
     */
    int readTimeoutMs() default 10_000;

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and Integer.MAX_VALUE when converted to milliseconds
     *
     * @return writeTimeoutMs
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

}
