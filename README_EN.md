
## retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://api.travis-ci.com/LianjiaTech/retrofit-spring-boot-starter.svg?branch=master)](https://travis-ci.com/github/LianjiaTech/retrofit-spring-boot-starter)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/v/release/lianjiatech/retrofit-spring-boot-starter.svg)](https://github.com/LianjiaTech/retrofit-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.5+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-chentianming-orange.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)
[![QQ-Group](https://img.shields.io/badge/QQ%E7%BE%A4-806714302-orange.svg?style=flat-square) ](https://img.ljcdn.com/hc-picture/HTTP-exception-information-formatter6302d742-ebc8-4649-95cf-62ccf57a1add)


[中文文档](https://github.com/LianjiaTech/retrofit-spring-boot-starter)

`Retrofit` is a type safe HTTP client for `Android` and `Java`. **Supporting HTTP requests through `interfaces`** is the strongest feature of `Retrofit`. `Spring-boot` is the most widely used java development framework, but there is no official `retrofit` support for rapid integration with `spring-boot` framework, so we developed `retrofit-spring-boot-starter`.

**`Retrofit-spring-boot-starter` realizes the rapid integration of `Retrofit` and `spring-boot`, supports many enhanced features and greatly simplifies development**.

🚀The project is in continuous optimization iteration. We welcome everyone to mention ISSUE and PR! Your star✨ is our power for continuous updating!

<!--more-->

## Features

- [x] [Custom injection OkHttpClient](#Custom-injection-OkHttpClient)
- [x] [Annotation interceptor](#Annotation-interceptor)
- [x] [Connection pool management](#Connection-pool-management)
- [x] [Log printing](#Log-printing)
- [x] [Request retry](#Request-retry)
- [x] [Error decoder](#Error-decoder)
- [x] [Global interceptor](#Global-interceptor)
- [x] [Fuse degrade](#Fuse-degrade)
- [x] [HTTP calls between microservices](#HTTP-calls-between-microservices)
- [x] [CallAdapter](#CallAdapter)
- [x] [Converter](#Converter)

## Quick start

### Introduce dependency

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.2.5</version>
</dependency>
```

This project depends on Retrofit-2.9.0, okhttp-3.14.9, and okio-1.17.5 versions. If there is a conflict, please manually import related jar packages. The complete dependencies are as follows:

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.2.5</version>
</dependency>
 <dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>logging-interceptor</artifactId>
    <version>3.14.9</version>
</dependency>
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>3.14.9</version>
</dependency>
<dependency>
    <groupId>com.squareup.okio</groupId>
    <artifactId>okio</artifactId>
    <version>1.17.5</version>
</dependency>
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>retrofit</artifactId>
    <version>2.9.0</version>
</dependency>
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>converter-jackson</artifactId>
    <version>2.9.0</version>
</dependency>
```

### Define HTTP interface

**The interface must be marked with `@RetrofitClient` annotation**! Related annotations of HTTP can refer to the official documents: [Retrofit official documents](https://square.github.io/retrofit/).

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
```

### Inject and use

**Inject the interface into other Service and use!**

```java
@Service
public class TestService {

    @Autowired
    private HttpApi httpApi;

    public void test() {
        // Initiate HTTP request via HTTP Api
    }
}
```

**By default, the SpringBoot scan path is automatically used for retrofitClient registration**. You can also add `@RetrofitScan` to the configuration class to manually specify the scan path.

## Related annotations of HTTP request

All of the related annotations of `HTTP` request use native annotations of `retrofit`. **For more information, please refer to the official document: [Retrofit official documents](https://square.github.io/retrofit/)**. The following is a brief description:

| Annotation classification|Supported annotations |
|------------|-----------|
|Request method|`@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP`|
|Request header|`@Header` `@HeaderMap` `@Headers`|
|Query param|`@Query` `@QueryMap` `@QueryName`|
|Path param|`@Path`|
|Form-encoded param|`@Field` `@FieldMap` `@FormUrlEncoded`|
| Request body |`@Body`|
|File upload|`@Multipart` `@Part` `@PartMap`|
|Url param|`@Url`|

## Configuration item description

`Retrofit-spring-boot-starter` supports multiple configurable properties to deal with different business scenarios.For more information, please refer to the [configuration example](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/src/test/resources/application.yml)

## Advanced feature

### Custom injection OkHttpClient

In general, dynamic creation of `OkHttpClient` object through the `@RetrofitClient` annotation can satisfy most usage scenarios. But in some cases, users may need to customize `OkHttpClient`. At this time, you can define a static method with the return type of `OkHttpClient.Builder` on the interface to achieve this. The code example is as follows:

```java
@RetrofitClient(baseUrl = "http://ke.com")
public interface HttpApi3 {

    @OkHttpClientBuilder
    static OkHttpClient.Builder okhttpClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS);

    }

    @GET
    Result<Person> getPerson(@Url String url, @Query("id") Long id);
}
```

> The method must be marked with `@OkHttpClientBuilder` annotation!



### Annotation interceptor

In many cases, we hope that certain http requests in a certain interface execute a unified interception processing logic. So as to support this feature, `retrofit-spring-boot-starter` provide **annotation interceptor** and at the same time achieves **matching interception based on URL path**. The use is mainly divided into 2 steps:

1. Inherit `BasePathMatchInterceptor` and write interceptor processor;
2. Mark the interface with `@Intercept`.

> To configure multiple interceptors, just mark multiple `@Intercept` annotations on the interface!

The following is an example of how to use annotation interceptors *by splicing timestamp after the URL of a specified request*.

#### Inherit `BasePathMatchInterceptor` and write interceptor processor

```java
@Component
public class TimeStampInterceptor extends BasePathMatchInterceptor {

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        long timestamp = System.currentTimeMillis();
        HttpUrl newUrl = url.newBuilder()
                .addQueryParameter("timestamp", String.valueOf(timestamp))
                .build();
        Request newRequest = request.newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(newRequest);
    }
}

```

#### Mark the interface with `@Intercept`

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = TimeStampInterceptor.class, include = {"/api/**"}, exclude = "/api/test/savePerson")
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

    @POST("savePerson")
    Result<Person> savePerson(@Body Person person);
}
```

The above `@Intercept`: Intercept the request under the path `/api/**` in the `HttpApi` interface (excluding `/api/test/savePerson`).The interception processor uses `TimeStampInterceptor`.

### Extended annotation interceptor

Sometimes, we need to dynamically pass in some parameters in the **intercept annotation** and then use these parameter when performing interception. In this case, we can extend the implementation of **custom intercept annotation**. You must mark `custom intercept annotation` with `@InterceptMark` and **the annotation must include `include(), exclude(), handler()` attribute information**. The use is mainly divided into 3 steps:

1. Custom intercept annotation
2. Inherit `BasePathMatchInterceptor` and write interceptor processor
3. Mark the interface with custom intercept annotation

For example, we need to **dynamically add the signature information of `accesskeyid` and `accesskeysecret` in the request header to initiate HTTP requests normally**. In this case, we can **customize a signature interceptor Annotation `@sign` to implement**.The following is an example of the custom `@sign` intercept annotation.


#### Custom `@sign` intercept annotation

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {
    /**
     * secret key
     * Support the configuration in the form of placeholder.
     *
     * @return
     */
    String accessKeyId();

    /**
     * secret key
     * Support the configuration in the form of placeholder.
     *
     * @return
     */
    String accessKeySecret();

    /**
     * Interceptor matching path.
     *
     * @return
     */
    String[] include() default {"/**"};

    /**
     * Interceptor excludes matching and intercepting by specified path.
     *
     * @return
     */
    String[] exclude() default {};

    /**
     * The interceptor class which handles the annotation.
     * Get the corresponding bean from the spring container firstly.If not, use
     * reflection to create one!
     *
     * @return
     */
    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

There are two points to be noted in the extension of the `custom intercept annotation`:

1. `Custom intercept annotation` must be marked with `@InterceptMark`.
2. The annotation must include `include(), exclude(), handler()` attribute information.

#### Realize `SignInterceptor`

```java
@Component
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
        return chain.proceed(newReq);
    }
}
```

**The above `accessKeyId` and `accessKeySecret` value will be automatically injected according to the `accessKeyId()` and `accessKeySecret()` values of the `@sign` annotation.If `@Sign` specifies a string in the form of a placeholder, the configuration property value will be taken for injection**. In addition, **`accessKeyId` and `accessKeySecret` value must provide `setter` method**.

#### Mark interface with `@Sign`

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", exclude = {"/api/test/person"})
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

    @POST("savePerson")
    Result<Person> savePerson(@Body Person person);
}
```

In this way, the signature information can be automatically added to the request of the specified URL.

### Connection pool management

By default, all HTTP requests sent through `Retrofit` will use the default connection pool of `max idle connections = 5 keep alive second = 300`. Of course, We can also configure multiple custom connection pools in the configuration file and then specify the usage through the `poolName` attribute of `@retrofitclient`. For example, we want to make all requests under an interface use the connection pool of `poolName = test1`. The code implementation is as follows:

1. Configure the connection pool.

    ```yaml
    retrofit:
      # 连接池配置
      pool:
        # test1连接池配置
        test1:
          # 最大空闲连接数
          max-idle-connections: 3
          # 连接保活时间(秒)
          keep-alive-second: 100
    ```

2. Use the `poolName` property of `@Retrofitclient` to specify the connection pool to be used.
    ```java
    @RetrofitClient(baseUrl = "${test.baseUrl}", poolName="test1")
    public interface HttpApi {

        @GET("person")
        Result<Person> getPerson(@Query("id") Long id);
    }
    ```

### Log printing

In many cases, we want to record the http request log. You can global control whether the log is enabled through `retrofit.log.enable` configuration. For each interface, you can control whether to enable it through the `enableLog` of `@RetrofitClient`. You can specify the log printing level and log printing strategy of each interface through `logLevel` and `logStrategy`. `Retrofit-spring-boot-starter` supports five log printing levels( `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`) which default to `INFO` and four log printing strategy( `NONE`, `BASIC`, `HEADERS`, `BODY`) which default to `BASIC`. The meanings of the 4 log printing strategies are as follows:

1. `NONE`：No logs.
2. `BASIC`：Logs request and response lines.
3. `HEADERS`：Logs request and response lines and their respective headers.
4. `BODY`：Logs request and response lines and their respective headers and bodies (if present).

By default, `retrofit-spring-boot-starter` uses `DefaultLoggingInterceptor` to perform the real log printing function. The bottom is `okhttp` native `HttpLoggingInterceptor`. Of course, you can also customize and implement your own log printing interceptor by simply inheriting the `baselogginginterceptor`( For details, please refer to the implementation of `defaultlogginginterceptor`), and then configure it in the configuration file.
```yaml
retrofit:
  log:
    enable: true
    logging-interceptor: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor
```

### Request retry

`retrofit-spring-boot-starter` supports global retry and declarative retry.

#### global retry

Global retry is enabled by default and can be disabled by configuring `retrofit.retry.enable-global-retry=false`. After enabling, all `HTTP` requests will be retried automatically according to the configuration parameters. The detailed configuration items are as follows:

```yaml
retrofit:
  # 重试配置
  retry:
    # 是否启用全局重试
    enable-global-retry: true
    # 全局重试间隔时间
    global-interval-ms: 20
    # 全局最大重试次数
    global-max-retries: 10
    # 全局重试规则
    global-retry-rules:
      - response_status_not_2xx
    # 重试拦截器
    retry-interceptor: com.github.lianjiatech.retrofit.spring.boot.retry.DefaultRetryInterceptor
```

#### declarative retry

If you only need to specify certain requests before retrying, you can use declarative retry! Specifically, declare the `@Retry` annotation on the interface or method.


### Error decoder

When an error occurs in the `HTTP` request (including an exception or the response data does not meet expectations), the error decoder can decode `HTTP` related information into a custom exception. You can specify the error decoder of the current interface in the `errorDecoder()` annotated by `@RetrofitClient`. The custom error decoder needs to implement the `ErrorDecoder` interface:

```java
/**
 * When an exception occurs in the request or an invalid response result is received, the HTTP related information is decoded into the exception,
 * and the invalid response is determined by the business itself.
 *
 * @author Tianming Chen
 */
public interface ErrorDecoder {

    /**
     * When the response is invalid, decode the HTTP information into the exception, invalid response is determined by business.
     *
     * @param request  request
     * @param response response
     * @return If it returns null, the processing is ignored and the processing continues with the original response.
     */
    default RuntimeException invalidRespDecode(Request request, Response response) {
        if (!response.isSuccessful()) {
            throw RetrofitException.errorStatus(request, response);
        }
        return null;
    }


    /**
     * When an IO exception occurs in the request, the HTTP information is decoded into the exception.
     *
     * @param request request
     * @param cause   IOException
     * @return RuntimeException
     */
    default RuntimeException ioExceptionDecode(Request request, IOException cause) {
        return RetrofitException.errorExecuting(request, cause);
    }

    /**
     * When the request has an exception other than the IO exception, the HTTP information is decoded into the exception.
     *
     * @param request request
     * @param cause   Exception
     * @return RuntimeException
     */
    default RuntimeException exceptionDecode(Request request, Exception cause) {
        return RetrofitException.errorUnknown(request, cause);
    }

}

```

## Global interceptor

### Global application interceptor

If we need to implement unified interception processing for HTTP requests of the whole system, we can customize the implementation of global interceptor `BaseGlobalInterceptor` and configure it as a `Bean` in `Spring`! For example, we need to carry source information for all http requests initiated in the entire system.

```java
@Component
public class SourceInterceptor extends BaseGlobalInterceptor {
    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("source", "test")
                .build();
        return chain.proceed(newReq);
    }
}
```

### Global network interceptor

You only need to implement the NetworkInterceptor interface and configure it as a bean in the spring container to support automatic weaving into the global network interceptor.

### Fuse degrade

In the distributed service architecture, fuse downgrade of unstable external services is one of the important measures to ensure high service availability. Since the stability of external services cannot be guaranteed, when external services are unstable, the response time will become longer. Correspondingly, the caller's response time will become longer, threads will accumulate, and eventually the caller's thread pool may be exhausted, causing the entire service to be unavailable. Therefore, we need to fuse and downgrade unstable weakly dependent service calls, temporarily cut off unstable calls, and avoid local instability leading to an overall service avalanche.

retrofit-spring-boot-starter supports the fuse downgrade function, and the bottom layer is based on [Sentinel](https://sentinelguard.io/zh-cn/docs/introduction.html). Specifically, it supports self-discovery of fusing resources and annotated degrade rule configuration. If you need to use the fuse to downgrade, you only need to do the following:

#### 1. Enable fuse degrade

By default, the fuse downgrade function is turned off, you need to set the corresponding configuration items to turn on the fuse downgrade function

```yaml
retrofit:
  enable-degrade: true
  # the degade type(Currently only Sentinel is supported)
  degrade-type: sentinel
  # the resource name parser
  resource-name-parser: com.github.lianjiatech.retrofit.spring.boot.degrade.DefaultResourceNameParser
```

The resource name resolver is used to implement user-defined resource names. The default configuration is `DefaultResourceNameParser`, and the corresponding resource name format is `HTTP_OUT:GET:http://localhost:8080/api/degrade/test`.Users can inherit the `BaseResourceNameParser` class to implement their own resource name parser.

**In addition, since the fuse downgrade function is optional, enabling fuse downgrade requires users to introduce Sentinel dependencies by themselves**:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.6.3</version>
</dependency>
```

### Configure degrade rules (optional)

**`retrofit-spring-boot-starter` supports annotation-based configuration of downgrade rules, and you can configure downgrade rules through @Degrade annotations**. The @Degrade annotation can be configured on the interface or method, and the priority of the configuration on the method is higher.


```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Degrade {

    /**
     * RT threshold or exception ratio threshold count.
     */
    double count();

    /**
     * Degrade recover timeout (in seconds) when degradation occurs.
     */
    int timeWindow() default 5;

    /**
     * Degrade strategy (0: average RT, 1: exception ratio).
     */
    DegradeStrategy degradeStrategy() default DegradeStrategy.AVERAGE_RT;
}
```

> **If the application project already supports the configuration of downgrade rules through the configuration center, you can ignore the annotation configuration method**。

#### 3. @RetrofitClient set fallback or fallbackFactory (optional)

**If `@RetrofitClient` does not set `fallback` or `fallbackFactory`, when the fuse is triggered, `RetrofitBlockException` will be thrown directly. The user can customize the return value of the method when fusing by setting `fallback` or `fallbackFactory`**. The `fallback` class must be the implementation class of the current interface, `fallbackFactory` must be the `FallbackFactory<T>` implementation class, and the generic parameter type is the current interface type. In addition, fallback and fallbackFactory instances must be configured as Spring container beans.

**The main difference between `fallbackFactory` and `fallback` is that it can sense the cause of each fusing**. The reference example is as follows:

```java
@Slf4j
@Service
public class HttpDegradeFallback implements HttpDegradeApi {

    @Override
    public Result<Integer> test() {
        Result<Integer> fallback = new Result<>();
        fallback.setCode(100)
                .setMsg("fallback")
                .setBody(1000000);
        return fallback;
    }
}
```

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    /**
     * Returns an instance of the fallback appropriate for the given cause
     *
     * @param cause fallback cause
     * @return 实现了retrofit接口的实例。an instance that implements the retrofit interface.
     */
    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("触发熔断了! ", cause.getMessage(), cause);
        return new HttpDegradeApi() {
            @Override
            public Result<Integer> test() {
                Result<Integer> fallback = new Result<>();
                fallback.setCode(100)
                        .setMsg("fallback")
                        .setBody(1000000);
                return fallback;
            }
    }
}
```


####  



### HTTP calls between microservices

**By configuring the `serviceId` and `path` properties of `@Retrofit`, HTTP calls between microservices can be realized**.

```java
@RetrofitClient(serviceId = "${jy-helicarrier-api.serviceId}", path = "/m/count", errorDecoder = HelicarrierErrorDecoder.class)
@Retry
public interface ApiCountService {

}
```

Users need to implement the `ServiceInstanceChooser` interface by themselves, complete the selection logic of the service instance, and configure it as the `Bean` of the `Spring` container.
For `Spring Cloud` applications, `retrofit-spring-boot-starter` provides the implementation of `SpringCloudServiceInstanceChooser`, Users only need to configure it as the `Bean` of `Spring`.

```java
public interface ServiceInstanceChooser {

    /**
     * Chooses a ServiceInstance URI from the LoadBalancer for the specified service.
     *
     * @param serviceId The service ID to look up the LoadBalancer.
     * @return Return the uri of ServiceInstance
     */
    URI choose(String serviceId);

}
```

```java
@Bean
@Autowired
public ServiceInstanceChooser serviceInstanceChooser(LoadBalancerClient loadBalancerClient) {
    return new SpringCloudServiceInstanceChooser(loadBalancerClient);
}
```


## CallAdapter and Converter

You only need to implement the `NetworkInterceptor` interface and configure it as a `bean` in the `spring` container to support automatic weaving into the global network interceptor.

### CallAdapter

`Retrofit` can adapt the `Call<T>` object to the return value type of the interface method by calling the adapter `CallAdapterFactory`. `Retrofit-spring-boot-starter` extends two implementations of `CallAdapterFactory`:
1. `BodyCallAdapterFactory`
    - Feature is enabled by default, and can be disabled by configuring `retrofit.enable-body-call-adapter=false`.
    - Execute the http request synchronously and adapt the response body to an instance of the return value type of the interface method.
    - All return types can use this adapter except `Retrofit.Call<T>`, `Retrofit.Response<T>`, `java.util.concurrent.CompletableFuture<T>`.
2. `ResponseCallAdapterFactory`
    - Feature is enabled by default, and can be disabled by configuring `retrofit.enable-response-call-adapter=false`.
    - Execute the http request synchronously, adapt the response body content to `Retrofit.Response<T>` and return.
    - If the return value type of the method is `Retrofit.Response<T>`, you can use this adapter.

**Retrofit automatically selects the corresponding `CallAdapterFactory` to perform adaptation processing according to the method return value type! With the default `CallAdapterFactory` of retrofit, it can support various types of method return values:**

- `Call<T>`: Do not perform adaptation processing, directly return the `Call<T>` object.
- `CompletableFuture<T>`: Adapt the response body content to a `CompletableFuture<T>` object and return.
- `Void`: You can use `Void` regardless of the return type. If the http status code is not 2xx, just throw an error!
- `Response<T>`: Adapt the response content to a `Response<T>` object and return.
- Any other Java type: Adapt the response body content to a corresponding Java type object and return. If the http status code is not 2xx, just throw an error!

```java
    /**
     * Call<T>
     * do not perform adaptation processing, directly return the Call<T> object.
     * @param id
     * @return
     */
    @GET("person")
    Call<Result<Person>> getPersonCall(@Query("id") Long id);

    /**
     *  CompletableFuture<T>
     *  Adapt the response body content to a CompletableFuture<T> object and return.
     * @param id
     * @return
     */
    @GET("person")
    CompletableFuture<Result<Person>> getPersonCompletableFuture(@Query("id") Long id);

    /**
     * Void
     * You can use Void regardless of the return type. If the http status code is not 2xx, just throw an error!
     * @param id
     * @return
     */
    @GET("person")
    Void getPersonVoid(@Query("id") Long id);

    /**
     *  Response<T>
     * Adapt the response content to a Response<T> object and return.
     * @param id
     * @return
     */
    @GET("person")
    Response<Result<Person>> getPersonResponse(@Query("id") Long id);

    /**
     * Any other Java type
     * Adapt the response body content to a corresponding Java type object and return. If the http status code is not 2xx, just throw an error!
     * @param id
     * @return
     */
    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

```

**We can also implement our own `CallAdapter`** by inheriting the `CallAdapter.Factory`!

`retrofit-spring-boot-starter` supports configuring the global `CallAdapter.Factory` through `retrofit.global-call-adapter-factories`. The call adapter factory instance is first obtained from the Spring container. If it is not obtained, it is created by reflection. The default global call adapter factory is `[BodyCallAdapterFactory, ResponseCallAdapterFactory]`.

```yaml
retrofit:
  global-call-adapter-factories:
    - com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory
    - com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory
```

For each Java interface, you can also specify the `CallAdapter.Factory` used by the current interface through `callAdapterFactories()` annotated by `@RetrofitClient`, and the specified call adapter factory instance is still preferentially obtained from the Spring container.

**Note: If `CallAdapter.Factory` does not have a parameterless constructor of `public`, please manually configure it as the `Bean` object of the `Spring` container**!


### Converter

`Retrofit` uses `Converter` to convert the object annotated with `@Body` into the request body, and the response body data into a `Java` object. The following types of `Converter` can be used:

- [Gson](https://github.com/google/gson): com.squareup.Retrofit:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.Retrofit:converter-jackson
- [Moshi](https://github.com/square/moshi/): com.squareup.Retrofit:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.Retrofit:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.Retrofit:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.Retrofit:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- fastJson：com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

`retrofit-spring-boot-starter` supports configuring the global `converter factory` through `retrofit.global-converter-factories`. The converter factory instance is first obtained from the Spring container. If it is not obtained, it is created by reflection. The default global data converter factory is `retrofit2.converter.jackson.JacksonConverterFactory`, you can directly configure the `jackson` serialization rules through `spring.jackson.*`, please refer to [Customize the Jackson ObjectMapper](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/#howto-customize-the-jackson-objectmapper)

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

For each Java interface, you can also specify the `Converter.Factory` used by the current interface through `converterFactories()` annotated by `@RetrofitClient`, and the specified converter factory instance is still preferentially obtained from the Spring container.

**Note: If `Converter.Factory` does not have a parameterless constructor of `public`, please manually configure it as the `Bean` object of the `Spring` container**!


## Other features

### Upload file example

#### Build MultipartBody.Part

```java
// Encode file names with URLEncoder
String fileName = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "utf-8");
okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse("multipart/form-data"),file.getBytes());
MultipartBody.Part file = MultipartBody.Part.createFormData("file", fileName, requestBody);
apiService.upload(file);
```

#### Http upload interface

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);

```

### download file

#### http download interface

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}

```

#### http download usage

```java
@SpringBootTest(classes = RetrofitTestApplication.class)
@RunWith(SpringRunner.class)
public class DownloadTest {

    @Autowired
    DownloadApi downLoadApi;

    @Test
    public void download() throws Exception {
        String fileKey = "6302d742-ebc8-4649-95cf-62ccf57a1add";
        Response<ResponseBody> response = downLoadApi.download(fileKey);
        ResponseBody responseBody = response.body();
        // InputStream
        InputStream is = responseBody.byteStream();

        // The specific handling of binary streams is controlled by the business itself. Here is an example of writing a file.
        File tempDirectory = new File("temp");
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
        }
        File file = new File(tempDirectory, UUID.randomUUID().toString());
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        byte[] b = new byte[1024];
        int length;
        while ((length = is.read(b)) > 0) {
            fos.write(b, 0, length);
        }
        is.close();
        fos.close();
    }
}
```

### Dynamic URL example

Realize dynamic URL through `@url` annotation

**Note: `@url` must be placed in the first position of the method parameter. The original definition of `@GET`, `@POST` and other annotations do not need to define the endpoint path**!

```java
 @GET
 Map<String, Object> test3(@Url String url,@Query("name") String name);

```

## Feedback

If you have any questions, welcome to raise issue or add QQ group to feedback.

QQ Group Number: 806714302

![QQ Group](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/qun.png)
