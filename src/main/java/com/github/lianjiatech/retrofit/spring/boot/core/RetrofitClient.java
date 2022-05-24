package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
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
     */
    String baseUrl() default Constants.STR_EMPTY;

    /**
     * The name of the service.
     * Can be specified as property key, eg: ${propertyKey}.
     */
    String serviceId() default Constants.STR_EMPTY;

    /**
     * Path prefix to be used by all method-level mappings.
     */
    String path() default Constants.STR_EMPTY;

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
     * 当前接口采用的错误解码器，当请求发生异常或者收到无效响应结果的时候，将HTTP相关信息解码到异常中，无效响应由业务自己判断。
     * 一般情况下，每个服务对应的无效响应各不相同，可以自定义对应的{@link ErrorDecoder}，然后配置在这里。<br></>
     * The error decoder used in the current interface will decode HTTP related information into the exception when an exception occurs in the request or an invalid response result is received.
     * The invalid response is determined by the business itself.
     * In general, the invalid response corresponding to each service is different, you can customize the corresponding {@link ErrorDecoder}, and then configure it here.
     *
     */
    Class<? extends ErrorDecoder> errorDecoder() default ErrorDecoder.DefaultErrorDecoder.class;

    /**
     * When calling {@link Retrofit#create(Class)} on the resulting {@link Retrofit} instance, eagerly validate the
     * configuration of all methods in the supplied interface.
     *
     * @return validateEagerly
     */
    boolean validateEagerly() default false;

    /**
     * 原始OkHttpClient，根据该名称到#{@link SourceOkHttpClientRegistry}查找对应的OkHttpClient来构建当前接口的OkhttpClient。
     */
    String sourceOkHttpClient() default Constants.DEFAULT_SOURCE_OK_HTTP_CLIENT;

}
