
## 简介

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://api.travis-ci.com/LianjiaTech/retrofit-spring-boot-starter.svg?branch=master)](https://travis-ci.com/github/LianjiaTech/retrofit-spring-boot-starter)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/v/release/lianjiatech/retrofit-spring-boot-starter.svg)](https://github.com/LianjiaTech/retrofit-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.x+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-chentianming-orange.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)
[![QQ-Group](https://img.shields.io/badge/QQ%E7%BE%A4-806714302-orange.svg?style=flat-square) ](https://img.ljcdn.com/hc-picture/6302d742-ebc8-4649-95cf-62ccf57a1add)

众所周知，`Retrofit`是适用于`Android`和`Java`且类型安全的HTTP客户端，其最大的特性的是**支持通过`接口`的方式发起HTTP请求**。而`spring-boot`是使用最广泛的Java开发框架，但是`Retrofit`官方没有支持与`spring-boot`框架快速整合，因此我们开发了`retrofit-spring-boot-starter`。

**`retrofit-spring-boot-starter`实现了`Retrofit`与`spring-boot`框架快速整合，并且支持了诸多功能增强，极大简化开发**。

🚀项目持续优化迭代，欢迎大家提ISSUE和PR！造轮子不易，麻烦大家给一颗star✨，您的star是我们持续更新的动力！

<!--more-->

## 功能特性

- [x] [自定义注入OkHttpClient](#自定义注入OkHttpClient)
- [x] [注解式拦截器](#注解式拦截器)
- [x] [连接池管理](#连接池管理)
- [x] [日志打印](#日志打印)
- [x] [请求重试](#请求重试)
- [x] [错误解码器](#错误解码器)
- [x] [全局拦截器](#全局拦截器)
- [x] [调用适配器](#调用适配器)
- [x] [数据转换器](#数据转码器)

## 快速使用

### 引入依赖

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.1.3</version>
</dependency>
```

### 配置`@RetrofitScan`注解

你可以给带有 `@Configuration` 的类配置`@RetrofitScan`，或者直接配置到`spring-boot`的启动类上，如下：

```java
@SpringBootApplication
@RetrofitScan("com.github.lianjiatech.retrofit.spring.boot.test")
public class RetrofitTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrofitTestApplication.class, args);
    }
}
```

### 定义http接口

**接口必须使用`@RetrofitClient`注解标记**！http相关注解可参考官方文档：[retrofit官方文档](https://square.github.io/retrofit/)。

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
```

### 注入使用

**将接口注入到其它Service中即可使用！**

```java
@Service
public class TestService {

    @Autowired
    private HttpApi httpApi;

    public void test() {
        // 通过httpApi发起http请求
    }
}
```

## HTTP请求相关注解

`HTTP`请求相关注解，全部使用了`retrofit`原生注解。**详细信息可参考官方文档：[retrofit官方文档](https://square.github.io/retrofit/)**，以下是一个简单说明。

| 注解分类|支持的注解 |
|------------|-----------|
|请求方式|`@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS`|
|请求头|`@Header` `@HeaderMap` `@Headers`|
|Query参数|`@Query` `@QueryMap` `@QueryName`|
|path参数|`@Path`|
|path参数|`@Path`|
|form-encoded参数|`@Field` `@FieldMap` `@FormUrlEncoded`|
|文件上传|`@Multipart` `@Part` `@PartMap`|
|url参数|`@Url`|

## 配置项说明

`retrofit-spring-boot-starter`支持了多个可配置的属性，用来应对不同的业务场景。您可以视情况进行修改，具体说明如下：

| 配置|默认值 | 说明 |
|------------|-----------|--------|
| enable-body-call-adapter | true| 是否启用 BodyCallAdapter适配器 |
| enable-response-call-adapter | true| 是否启用 ResponseCallAdapter适配器 |
| enable-log | true| 启用日志打印 |
|logging-interceptor | DefaultLoggingInterceptor | 日志打印拦截器 |
| pool | | 连接池配置 |
| disable-void-return-type | false | 禁用java.lang.Void返回类型 |
| http-exception-message-formatter | DefaultHttpExceptionMessageFormatter | Http异常信息格式化器 |
| retry-interceptor | DefaultRetryInterceptor | 请求重试拦截器 |

`yml`配置方式：

```yaml
retrofit:
  # 是否启用 BodyCallAdapter适配器
  enable-body-call-adapter: true
  # 是否启用 ResponseCallAdapter适配器
  enable-response-call-adapter: true
  # 启用日志打印
  enable-log: true
  # 连接池配置
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
  # 禁用void返回值类型
  disable-void-return-type: false
  # 日志打印拦截器
  logging-interceptor: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultLoggingInterceptor
  # Http异常信息格式化器
  http-exception-message-formatter: com.github.lianjiatech.retrofit.spring.boot.interceptor.DefaultHttpExceptionMessageFormatter
  # 请求重试拦截器
  retry-interceptor: com.github.lianjiatech.retrofit.spring.boot.retry.DefaultRetryInterceptor
```

## 高级功能

### 自定义注入OkHttpClient

通常情况下，通过`@RetrofitClient`注解属性动态创建`OkHttpClient`对象能够满足大部分使用场景。但是在某些情况下，用户可能需要自定义`OkHttpClient`，这个时候，可以在接口上定义返回类型是`OkHttpClient.Builder`的静态方法来实现。代码示例如下：

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

> 方法必须使用`@OkHttpClientBuilder`注解标记！

### 注解式拦截器

很多时候，我们希望某个接口下的某些http请求执行统一的拦截处理逻辑。为了支持这个功能，`retrofit-spring-boot-starter`提供了**注解式拦截器**，同时做到了**基于url路径的匹配拦截**。使用的步骤主要分为2步：

1. 继承`BasePathMatchInterceptor`编写拦截处理器；
2. 接口上使用`@Intercept`进行标注。

下面以*给指定请求的url后面拼接timestamp时间戳*为例，介绍下如何使用注解式拦截器。

#### 继承`BasePathMatchInterceptor`编写拦截处理器

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

#### 接口上使用`@Intercept`进行标注

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

上面的`@Intercept`配置表示：拦截`HttpApi`接口下`/api/**`路径下（排除`/api/test/savePerson`）的请求，拦截处理器使用`TimeStampInterceptor`。

### 扩展注解式拦截器

有的时候，我们需要在**拦截注解**动态传入一些参数，然后再执行拦截的时候需要使用这个参数。这种时候，我们可以扩展实现**自定义拦截注解**。`自定义拦截注解`必须使用`@InterceptMark`标记，并且**注解中必须包括`include()、exclude()、handler()`属性信息**。使用的步骤主要分为3步：

1. 自定义拦截注解
2. 继承`BasePathMatchInterceptor`编写拦截处理器
3. 接口上使用自定义拦截注解；

例如我们需要**在请求头里面动态加入`accessKeyId`、`accessKeySecret`签名信息才能正常发起http请求**，这个时候**可以自定义一个加签拦截器注解`@Sign`来实现**。下面以自定义`@Sign`拦截注解为例进行说明。


#### 自定义`@Sign`注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {
    /**
     * 密钥key
     * 支持占位符形式配置。
     *
     * @return
     */
    String accessKeyId();

    /**
     * 密钥
     * 支持占位符形式配置。
     *
     * @return
     */
    String accessKeySecret();

    /**
     * 拦截器匹配路径
     *
     * @return
     */
    String[] include() default {"/**"};

    /**
     * 拦截器排除匹配，排除指定路径拦截
     *
     * @return
     */
    String[] exclude() default {};

    /**
     * 处理该注解的拦截器类
     * 优先从spring容器获取对应的Bean，如果获取不到，则使用反射创建一个！
     *
     * @return
     */
    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

扩展`自定义拦截注解`有以下2点需要注意：

1. `自定义拦截注解`必须使用`@InterceptMark`标记。
2. 注解中必须包括`include()、exclude()、handler()`属性信息。

#### 实现`SignInterceptor`

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

**上述`accessKeyId`和`accessKeySecret`字段值会依据`@Sign`注解的`accessKeyId()`和`accessKeySecret()`值自动注入，如果`@Sign`指定的是占位符形式的字符串，则会取配置属性值进行注入**。另外，**`accessKeyId`和`accessKeySecret`字段必须提供`setter`方法**。

#### 接口上使用`@Sign`

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

这样就能再指定url的请求上，自动加上签名信息了。

### 连接池管理

默认情况下，所有通过`Retrofit`发送的http请求都会使用`max-idle-connections=5  keep-alive-second=300`的默认连接池。当然，我们也可以在配置文件中配置多个自定义的连接池，然后通过`@RetrofitClient`的`poolName`属性来指定使用。比如我们要让某个接口下的请求全部使用`poolName=test1`的连接池，代码实现如下：

1. 配置连接池。

    ```yaml
    retrofit:
        # 连接池配置
        pool:
            test1:
            max-idle-connections: 3
            keep-alive-second: 100
            test2:
            max-idle-connections: 5
            keep-alive-second: 50
    ```

2. 通过`@RetrofitClient`的`poolName`属性来指定使用的连接池。

    ```java
    @RetrofitClient(baseUrl = "${test.baseUrl}", poolName="test1")
    public interface HttpApi {

        @GET("person")
        Result<Person> getPerson(@Query("id") Long id);
    }
    ```

### 日志打印

很多情况下，我们希望将http请求日志记录下来。通过`retrofit.enableLog`配置可以全局控制日志是否开启。
针对每个接口，可以通过`@RetrofitClient`的`enableLog`控制是否开启，通过`logLevel`和`logStrategy`，可以指定每个接口的日志打印级别以及日志打印策略。`retrofit-spring-boot-starter`支持了5种日志打印级别(`ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`)，默认`INFO`；支持了4种日志打印策略（`NONE`, `BASIC`, `HEADERS`, `BODY`），默认`BASIC`。4种日志打印策略含义如下：

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

### 错误解码器

在`HTTP`发生请求错误(包括发生异常或者响应数据不符合预期)的时候，错误解码器可将`HTTP`相关信息解码到自定义异常中。你可以在`@RetrofitClient`注解的`errorDecoder()`指定当前接口的错误解码器，自定义错误解码器需要实现`ErrorDecoder`接口：

```java
/**
 * 错误解码器。ErrorDecoder.
 * 当请求发生异常或者收到无效响应结果的时候，将HTTP相关信息解码到异常中，无效响应由业务自己判断
 *
 * When an exception occurs in the request or an invalid response result is received, the HTTP related information is decoded into the exception,
 * and the invalid response is determined by the business itself.
 *
 * @author 陈添明
 */
public interface ErrorDecoder {

    /**
     * 当无效响应的时候，将HTTP信息解码到异常中，无效响应由业务自行判断。
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
     * 当请求发生IO异常时，将HTTP信息解码到异常中。
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
     * 当请求发生除IO异常之外的其它异常时，将HTTP信息解码到异常中。
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

## 全局拦截器

### 全局应用拦截器

如果我们需要对整个系统的的http请求执行统一的拦截处理，可以自定义实现全局拦截器`BaseGlobalInterceptor`, 并配置成`spring`容器中的`bean`！例如我们需要在整个系统发起的http请求，都带上来源信息。

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

### 全局网络拦截器

只需要实现`NetworkInterceptor`接口 并配置成`spring`容器中的`bean`就支持自动织入全局网络拦截器。

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

