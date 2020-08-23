
## 简介

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://api.travis-ci.com/LianjiaTech/retrofit-spring-boot-starter.svg?branch=master)](https://travis-ci.com/github/LianjiaTech/retrofit-spring-boot-starter)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/v/release/lianjiatech/retrofit-spring-boot-starter.svg)](https://github.com/LianjiaTech/retrofit-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/springboot-1.x+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-chentianming-orange.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)
[![QQ-Group](https://img.shields.io/badge/QQ%E7%BE%A4-806714302-orange.svg?style=flat-square) ](https://img.ljcdn.com/hc-picture/6302d742-ebc8-4649-95cf-62ccf57a1add)

> As is known to us all, `Retrofit` is a type safe HTTP client for `Android` and `Java`. **Supporting HTTP requests through `interfaces`** is the strongest feature of `Retrofit`. `Spring-boot` is the most widely used java development framework, but there is no official `Retrofit` support for rapid integration with `spring-boot` framework, so we have developed `retrofit-spring-boot-starter`.

**`Retrofit-spring-boot-starter` realizes the rapid integration of `Retrofit` and `spring-boot`, supports many enhanced features and greatly simplifies development**.

| [Quick start](#Quick-start) | [Annotation interceptor](#Annotation-interceptor) | [Connection pool management](#Connection-pool-management) | [Log printing](#Log-printing) | [异常信息格式化](#Http异常信息格式化器) | [请求重试](#请求重试) |[全局拦截器](#全局拦截器) | [调用适配器](#调用适配器) | [数据转换器](#数据转码器) |

<!--more-->

## Quick-start

### Introduce dependency

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

### Configure `@RetrofitScan` annotation

You can configure `@Configuration` for the class with `@RetrofitScan`, or directly configure it to the startup class of `spring-boot`, as follows:

```java
@SpringBootApplication
@RetrofitScan("com.github.lianjiatech.retrofit.spring.boot.test")
public class RetrofitTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrofitTestApplication.class, args);
    }
}
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

**Inject the interface into other Service to use!**

```java
@Service
public class TestService {

    @Autowired
    private HttpApi httpApi;

    public void test() {
        // HTTP request via HTTP Api
    }
}
```

## Related annotations of HTTP request 

All of the related annotations of `HTTP` request use native annotations of `retrofit`. **For more information, please refer to the official document: [Retrofit official documents](https://square.github.io/retrofit/)**. The following is a brief description:

| Annotation classification|Supported annotations |
|------------|-----------|
|Request method|`@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS`|
|Request header|`@Header` `@HeaderMap` `@Headers`|
|Query param|`@Query` `@QueryMap` `@QueryName`|
|Path param|`@Path`|
|Form-encoded param|`@Field` `@FieldMap` `@FormUrlEncoded`|
|File upload|`@Multipart` `@Part` `@PartMap`|
|Url param|`@Url`|

## Configuration description

`Retrofit-spring-boot-starter` supports multiple configurable properties to deal with different business scenarios. You can modify it as appropriate, the specific instructions are as follows:

| Configuration|Default value | description |
|------------|-----------|--------|
| enable-body-call-adapter | true| Whether to enable the bodycalladapter |
| enable-response-call-adapter | true| Whether to enable ResponseCallAdapter |
| enable-log | true| Enable log printing |
|logging-interceptor | DefaultLoggingInterceptor | Log print interceptor |
| pool | | Connection pool configuration |
| disable-void-return-type | false | disable java.lang.Void return type |
| http-exception-message-formatter | DefaultHttpExceptionMessageFormatter | HTTP exception information formatter |
| retry-interceptor | DefaultRetryInterceptor | Retry Interceptor |

`yml` Configuration:

```yaml
retrofit:
  # Enable BodyCallAdapter
  enable-body-call-adapter: true
  # Enable ResponseCallAdapter
  enable-response-call-adapter: true
  # Enable log printing
  enable-log: true
  # Connection pool configuration
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
  # Disable java.lang.Void return type
  disable-void-return-type: false
  # Log print interceptor
  logging-interceptor: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor
  # HTTP exception information formatter
  http-exception-message-formatter: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultHttpExceptionMessageFormatter
  # Retry Interceptor
  retry-interceptor: com.github.lianjiatech.retrofit.spring.boot.retry.DefaultRetryInterceptor
```

## Advanced feature

### Annotation-interceptor

Many times we want some HTTP requests under a certain interface to implement unified interception processing logic. So as to support the feature, `retrofit-spring-boot-starter` provide **annotation interceptor** and **matching interception based on URL path**. There are two steps to use:

1. Inherit `BasePathMatchInterceptor` and write interceptor processor
2. Mark the interface with `@Intercept`

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

The above `@Intercept`: The annotation intercepts the request which id under the `/api/**` path and under the `HttpApi` interface (Exclude `/api/test/savePerson`).The interception processor uses `TimeStampInterceptor`.

### Extended annotation interceptor

Sometimes, we need to dynamically pass in some parameters in the **intercept annotation** and then use these parameter when performing interception. In this case, we can extend the implementation of **custom intercept annotation**.You must mark `custom intercept annotation` with `@InterceptMark` and **the annotation must include `include(), exclude(), handler()` attribute information**. There are three steps to use:

1. Custom intercept annotation
2. Inherit `BasePathMatchInterceptor` and write interceptor processor
3. Mark the interface with custom intercept annotation

For example, we need to **dynamically add the signature information of `accesskeyid` and `accesskeysecret` in the request header to initiate HTTP requests normally**. 这In this case, we can **customize a signature interceptor Annotation `@sign` to implement**.The following is an example of the custom `@sign` intercept annotation.


#### custom `@sign` intercept annotation

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

**The above `accessKeyId` and `accessKeySecret` field value will be automatically injected according to the `accessKeyId()` and `accessKeySecret()` values of the `@sign` annotation.If `@Sign` specifies a string in the form of a placeholder, the configuration property value will be taken for injection**. In addition, **`accessKeyId` and `accessKeySecret` field value must provide `setter` method**.

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

### Connection-pool-management

By default, all HTTP requests sent through `Retrofit` will use the default connection pool of `max idle connections = 5 keep alive second = 300`. Of course, We can also configure multiple custom connection pools in the configuration file and then specify the usage through the `poolName` attribute of `@retrofitclient`. For example, we want to make all requests under an interface use the connection pool of `poolName = test1`. The code implementation is as follows:

1. Configure the connection pool.

    ```yaml
    retrofit:
        # Connection pool configuration
        pool:
            test1:
            max-idle-connections: 3
            keep-alive-second: 100
            test2:
            max-idle-connections: 5
            keep-alive-second: 50
    ```

2. Use the `poolName` property of `@retrofitclient` to specify the connection pool to be used.
    ```java
    @RetrofitClient(baseUrl = "${test.baseUrl}", poolName="test1")
    public interface HttpApi {

        @GET("person")
        Result<Person> getPerson(@Query("id") Long id);
    }
    ```

### Log-printing

In many cases, we want to record the http request log. You can global control whether the log is enabled through `retrofit.enableLog` configuration. For each interface, you can control whether to enable it through the `enableLog` of `@RetrofitClient`. You can specify the log printing level and log printing strategy of each interface through `logLevel` and `logStrategy`. `Retrofit-spring-boot-starter` supports five log printing levels( `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`), default `INFO` and four log printing strategy( `NONE`, `BASIC`, `HEADERS`, `BODY`), default `BASIC`. The meanings of the 4 log printing strategies are as follows:

1. `NONE`：No logs.
2. `BASIC`：Logs request and response lines.
3. `HEADERS`：Logs request and response lines and their respective headers.
4. `BODY`：Logs request and response lines and their respective headers and bodies (if present).

`retrofit-spring-boot-starter`默认使用了`DefaultLoggingInterceptor`执行真正的日志打印功能，其底层就是`okhttp`原生的`HttpLoggingInterceptor`。当然，你也可以自定义实现自己的日志打印拦截器，只需要继承`BaseLoggingInterceptor`（具体可以参考`DefaultLoggingInterceptor`的实现），然后在配置文件中进行相关配置即可。

```yaml
retrofit:
  # 日志打印拦截器
  logging-interceptor: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor
```

### Http异常信息格式化器

当出现http请求异常时，原始的异常信息可能阅读起来并不友好，因此`retrofit-spring-boot-starter`提供了`Http异常信息格式化器`，用来美化输出http请求参数，默认使用`DefaultHttpExceptionMessageFormatter`进行请求数据格式化。你也可以进行自定义，只需要继承`BaseHttpExceptionMessageFormatter`，再进行相关配置即可。

```yaml
retrofit:
  # Http异常信息格式化器
  http-exception-message-formatter: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultHttpExceptionMessageFormatter
```

### 请求重试

`retrofit-spring-boot-starter`支持请求重试功能，只需要在接口或者方法上加上`@Retry`注解即可。**`@Retry`支持重试次数`maxRetries`、重试时间间隔`intervalMs`以及重试规则`retryRules`配置**。重试规则支持三种配置：

1. `RESPONSE_STATUS_NOT_2XX`：响应状态码不是`2xx`时执行重试；
2. `OCCUR_IO_EXCEPTION`：发生IO异常时执行重试；
3. `OCCUR_EXCEPTION`：发生任意异常时执行重试；

默认响应状态码不是`2xx`或者发生IO异常时自动进行重试。需要的话，你也可以继承`BaseRetryInterceptor`实现自己的请求重试拦截器，然后将其配置上去。

```yaml
retrofit:
  # 请求重试拦截器
  retry-interceptor: com.github.lianjiatech.retrofit.spring.boot.retry.DefaultRetryInterceptor
```

### 全局拦截器

如果我们需要对整个系统的的http请求执行统一的拦截处理，可以自定义实现全局拦截器`BaseGlobalInterceptor`, 并配置成`spring`中的`bean`！例如我们需要在整个系统发起的http请求，都带上来源信息。

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

## 调用适配器和数据转码器

### 调用适配器

`Retrofit`可以通过调用适配器`CallAdapterFactory`将`Call<T>`对象适配成接口方法的返回值类型。`retrofit-spring-boot-starter`扩展2种`CallAdapterFactory`实现：

1. `BodyCallAdapterFactory`
    - 默认启用，可通过配置`retrofit.enable-body-call-adapter=false`关闭
    - 同步执行http请求，将响应体内容适配成接口方法的返回值类型实例。
    - 除了`Retrofit.Call<T>`、`Retrofit.Response<T>`、`java.util.concurrent.CompletableFuture<T>`之外，其它返回类型都可以使用该适配器。
2. `ResponseCallAdapterFactory`
    - 默认启用，可通过配置`retrofit.enable-response-call-adapter=false`关闭
    - 同步执行http请求，将响应体内容适配成`Retrofit.Response<T>`返回。
    - 如果方法的返回值类型为`Retrofit.Response<T>`，则可以使用该适配器。

**Retrofit自动根据方法返回值类型选用对应的`CallAdapterFactory`执行适配处理！加上Retrofit默认的`CallAdapterFactory`，可支持多种形式的方法返回值类型：**

- `Call<T>`: 不执行适配处理，直接返回`Call<T>`对象
- `CompletableFuture<T>`: 将响应体内容适配成`CompletableFuture<T>`对象返回
- `Void`: 不关注返回类型可以使用`Void`。如果http状态码不是2xx，直接抛错！
- `Response<T>`: 将响应内容适配成`Response<T>`对象返回
- 其他任意Java类型： 将响应体内容适配成一个对应的Java类型对象返回，如果http状态码不是2xx，直接抛错！

```java
    /**
     * Call<T>
     * 不执行适配处理，直接返回Call<T>对象
     * @param id
     * @return
     */
    @GET("person")
    Call<Result<Person>> getPersonCall(@Query("id") Long id);

    /**
     *  CompletableFuture<T>
     *  将响应体内容适配成CompletableFuture<T>对象返回
     * @param id
     * @return
     */
    @GET("person")
    CompletableFuture<Result<Person>> getPersonCompletableFuture(@Query("id") Long id);

    /**
     * Void
     * 不关注返回类型可以使用Void。如果http状态码不是2xx，直接抛错！
     * @param id
     * @return
     */
    @GET("person")
    Void getPersonVoid(@Query("id") Long id);

    /**
     *  Response<T>
     *  将响应内容适配成Response<T>对象返回
     * @param id
     * @return
     */
    @GET("person")
    Response<Result<Person>> getPersonResponse(@Query("id") Long id);

    /**
     * 其他任意Java类型
     * 将响应体内容适配成一个对应的Java类型对象返回，如果http状态码不是2xx，直接抛错！
     * @param id
     * @return
     */
    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

```

**我们也可以通过继承`CallAdapter.Factory`扩展实现自己的`CallAdapter`**；然后将自定义的`CallAdapterFactory`配置成`spring`的`bean`！

> 自定义配置的`CallAdapter.Factory`优先级更高！

### 数据转码器

`Retrofit`使用`Converter`将`@Body`注解标注的对象转换成请求体，将响应体数据转换成一个`Java`对象，可以选用以下几种`Converter`：

- Gson: com.squareup.Retrofit:converter-gson
- Jackson: com.squareup.Retrofit:converter-jackson
- Moshi: com.squareup.Retrofit:converter-moshi
- Protobuf: com.squareup.Retrofit:converter-protobuf
- Wire: com.squareup.Retrofit:converter-wire
- Simple XML: com.squareup.Retrofit:converter-simplexml

`retrofit-spring-boot-starter`默认使用的是jackson进行序列化转换，你可以直接通过`spring.jackson.*`配置`jackson`序列化规则，配置可参考[Customize the Jackson ObjectMapper](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/#howto-customize-the-jackson-objectmapper)！**如果需要使用其它序列化方式，在项目中引入对应的依赖，再把对应的`ConverterFactory`配置成spring的bean即可**。

**我们也可以通过继承`Converter.Factory`扩展实现自己的`Converter`**；然后将自定义的`Converter.Factory`配置成`spring`的`bean`！

> 自定义配置的`Converter.Factory`优先级更高！


## 其他功能示例

### 上传文件示例

#### 构建MultipartBody.Part

```java
// 对文件名使用URLEncoder进行编码
String fileName = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "utf-8");
okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse("multipart/form-data"),file.getBytes());
MultipartBody.Part file = MultipartBody.Part.createFormData("file", fileName, requestBody);
apiService.upload(file);
```

#### http上传接口

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);

```

### 动态URL示例

使用`@url`注解可实现动态URL。

**注意：`@url`必须放在方法参数的第一个位置。原有定义`@GET`、`@POST`等注解上，不需要定义端点路径**！

```java
 @GET
 Map<String, Object> test3(@Url String url,@Query("name") String name);

```

## 反馈建议

如有任何问题，欢迎提issue或者加QQ群反馈。

群号：806714302

![QQ群图片](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/qun.png)

