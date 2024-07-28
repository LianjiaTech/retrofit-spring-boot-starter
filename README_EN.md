## retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://api.travis-ci.com/LianjiaTech/retrofit-spring-boot-starter.svg?branch=master)](https://travis-ci.com/github/LianjiaTech/retrofit-spring-boot-starter)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/v/release/lianjiatech/retrofit-spring-boot-starter.svg)](https://github.com/LianjiaTech/retrofit-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.5+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-chentianming-orange.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)
[![QQ-Group](https://img.shields.io/badge/QQ%E7%BE%A4-806714302-orange.svg?style=flat-square) ](https://img.ljcdn.com/hc-picture/6302d742-ebc8-4649-95cf-62ccf57a1add)

[中文文档](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/README.md)

**A spring-boot starter for retrofit, supports rapid integration and feature enhancements.**
1. *Spring Boot 3.x project，please use retrofit-spring-boot-starter 3.x*。
2. *Spring Boot 1.x/2.x project，please use retrofit-spring-boot-starter 2.x*。

> Open source is not easy, please give me a star⭐️


## Features

- [x] [Customize OkHttpClient](#Customize-OkHttpClient)
- [x] [Annotation Interceptor](#Annotation-Interceptor)
- [x] [Log Print](#Log-Print)
- [x] [Request Retry](#Request-Retry)
- [x] [Fusing Degrade](#Fusing-Degrade)
- [x] [Error Decoder](#Error-Decoder)
- [x] [HTTP Calls Between Microservices](#HTTP-Calls-Between-Microservices)
- [x] [Global Interceptor](#Global-Interceptor)
- [x] [Call Adapter](#Call-Adapter)
- [x] [Data Converter](#Data-Converter)
- [x] [Meta-annotation](#Meta-annotation)
- [x] [Other Examples](#Other-Examples)

## Quick Start

### Import Dependencies

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
   <artifactId>retrofit-spring-boot-starter</artifactId>
   <version>3.1.2</version>
</dependency>
```

### Define HTTP Interface

**Interfaces must be marked with the `@RetrofitClient` annotation**！For HTTP related annotations, please refer to the official documentation：[Retrofit official documentation](https://square.github.io/retrofit/).

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

   /**
    * 根据id查询用户姓名
    */
   @POST("getName")
   String getName(@Query("id") Long id);
}
```

> Notice：**The method request path should be cautiously used at the beginning of `/`**. For `Retrofit`, if `baseUrl=http://localhost:8080/api/test/` and the method request path is `person`, then the complete request path of the method is: `http://localhost: 8080/api/test/person`. If the method request path is `/person`, the complete request path of the method is: `http://localhost:8080/person`.



### Inject Using

**Inject the interface into other services to use**:

```java
@Service
public class BusinessService {

   @Autowired
   private UserService userService;

   public void doBusiness() {
      // call userService
   }
}
```

**Automatically use `Spring Boot` scan path for `RetrofitClient` registration by default**. You can also manually specify the scan path by adding `@RetrofitScan` to the configuration class.

##  HTTP Related Annotations

`HTTP` request related annotations, all use `Retrofit` native annotations, the following is a brief description:

| Classification | Supported Annotations |
|------------|-----------|
| Request Method |`@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP`|
| Request Header |`@Header` `@HeaderMap` `@Headers`|
| Query Parameter |`@Query` `@QueryMap` `@QueryName`|
| Path Parameter |`@Path`|
| Form-encoded Parameter |`@Field` `@FieldMap` `@FormUrlEncoded`|
| Request Body |`@Body`|
| File Upload |`@Multipart` `@Part` `@PartMap`|
| Url Parameter |`@Url`|

> For details, please refer to the official documentation:[Retrofit official documentation](https://square.github.io/retrofit/)


## Configuration Properties

The component supports multiple configurable properties to deal with different business scenarios. The specific supported configuration properties and default values are as follows:

```yaml
retrofit:
   global-converter-factories:
      - com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory
      - retrofit2.converter.jackson.JacksonConverterFactory
   global-call-adapter-factories:
   global-log:
      enable: true
      log-level: info
      log-strategy: basic
      aggregate: true

   global-retry:
      enable: false
      interval-ms: 100
      max-retries: 2
      retry-rules:
         - response_status_not_2xx
         - occur_io_exception

   global-timeout:
      read-timeout-ms: 10000
      write-timeout-ms: 10000
      connect-timeout-ms: 10000
      call-timeout-ms: 0
   degrade:
      degrade-type: none
      global-sentinel-degrade:
         enable: false
         # Threshold corresponding to each degrade policy. Average response time (ms), exceptions ratio (0-1), number of exceptions (1-N)
         count: 1000
         time-window: 5
         # Degradation strategy (0: average response time; 1: ratio of exceptions; 2: number of exceptions)
         grade: 0

      global-resilience4j-degrade:
         enable: false
         # Get CircuitBreakerConfig from {@link CircuitBreakerConfigRegistry} based on this name as a global circuit breaker configuration
         circuit-breaker-config-name: defaultCircuitBreakerConfig
   auto-set-prototype-scope-for-path-math-interceptor: true
```

## Advanced Features

### Timeout config

If you only need to modify the timeout time of `OkHttpClient`, you can modify it through the relevant fields of `@RetrofitClient`, or modify the global timeout configuration.

### Customize OkHttpClient

If you need to modify other configuration of `OkHttpClient`, you can do it by customizing `OkHttpClient`, the steps are as follows:

Implement the `SourceOkHttpClientRegistrar` interface and call the `SourceOkHttpClientRegistry#register()` method to register the `OkHttpClient`.

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

   @Override
   public void register(SourceOkHttpClientRegistry registry) {
      // 注册customOkHttpClient，超时时间设置为1s
      registry.register("customOkHttpClient", new OkHttpClient.Builder()
              .connectTimeout(Duration.ofSeconds(1))
              .writeTimeout(Duration.ofSeconds(1))
              .readTimeout(Duration.ofSeconds(1))
              .addInterceptor(chain -> chain.proceed(chain.request()))
              .build());
   }
}
```

2. Specify the `OkHttpClient` to be used by the current interface through `@RetrofitClient.sourceOkHttpClient`.

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

   /**
    * 根据id查询用户信息
    */
   @GET("getUser")
   User getUser(@Query("id") Long id);
}
```

> Note: The component will not use the specified `OkHttpClient` directly, but will create a new one based on that `OkHttpClient`.



### Annotation Interceptor

The component provides **Annotation Interceptor**, which supports interception based on url path matching. The steps used are as follows:

1. Inherit `BasePathMatchInterceptor`
2. Use the `@Intercept` annotation to specify the interceptor to use

> If you need to use multiple interceptors, you can mark multiple `@Intercept` annotations on the interface.

The following is an example of "splicing timestamp behind the specified request url" to introduce how to use annotation interceptors.

#### Inherit `BasePathMatchInterceptor`

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
   @Override
   protected Response doIntercept(Chain chain) throws IOException {
      Response response = chain.proceed(chain.request());
      // response的Header加上path.match
      return response.newBuilder().header("path.match", "true").build();
   }
}
```

By default, **component will automatically set `scope` of `BasePathMatchInterceptor` to `prototype`**.
This feature can be turned off by `retrofit.auto-set-prototype-scope-for-path-math-interceptor=false`. After closing, you need to manually set `scope` to `prototype`.

```java
@Component
@Scope("prototype")
public class PathMatchInterceptor extends BasePathMatchInterceptor {

}
```

#### Use the `@Intercept` annotation to specify the interceptor to use

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// @Intercept() 如果需要使用多个路径匹配拦截器，继续添加@Intercept即可
public interface InterceptorUserService {

   /**
    * 根据id查询用户姓名
    */
   @POST("getName")
   Response<String> getName(@Query("id") Long id);

   /**
    * 根据id查询用户信息
    */
   @GET("getUser")
   Response<User> getUser(@Query("id") Long id);

}
```

### Custom Interception Annotation

Sometimes, we need to dynamically pass in some parameters in the "Interception Annotation", and then use these parameters when intercepting. At this time, we can use "Custom Interception Annotation", the steps are as follows:

1. Custom annotation. The `@InterceptMark` tag must be used, and the `include, exclude, handler` fields must be included in the annotation.
2. inherit `BasePathMatchInterceptor`
3. Use custom annotation on interfaces

For example, we need to "dynamically add `accessKeyId` and `accessKeySecret` signature information in the request header to initiate an HTTP request", which can be achieved by customizing the `@Sign` annotation.

#### Custom `@Sign` Annotation

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {
   
    String accessKeyId();
    
    String accessKeySecret();
    
    String[] include() default {"/**"};
    
    String[] exclude() default {};
    
    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

The interceptor specified in the `@Sign` annotation is `SignInterceptor`.

#### Implement `SignInterceptor`

```java
@Component
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

   private String accessKeyId;

   private String accessKeySecret;

   @Override
   public Response doIntercept(Chain chain) throws IOException {
      Request request = chain.request();
      Request newReq = request.newBuilder()
              .addHeader("accessKeyId", accessKeyId)
              .addHeader("accessKeySecret", accessKeySecret)
              .build();
      Response response = chain.proceed(newReq);
      return response.newBuilder().addHeader("accessKeyId", accessKeyId)
              .addHeader("accessKeySecret", accessKeySecret).build();
   }
}
```

> Note: The `accessKeyId` and `accessKeySecret` fields must provide a `setter` method.

The `accessKeyId` and `accessKeySecret` field values of the interceptor will be automatically injected according to the `accessKeyId()` and `accessKeySecret()` values of the `@Sign` annotation, if `@Sign` specifies a string in the form of a placeholder , the configuration property value will be taken for injection.

#### Using `@Sign` on the interface

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

   /**
    * 查询所有用户信息
    */
   @GET("getAll")
   Response<List<User>> getAll();

}
```

### Log Print

Component support supports global log printing and declarative log printing.

#### Global Log Printing

By default, global log printing is enabled, and the default configuration is as follows:

```yaml
retrofit:
   global-log:
      enable: true
      log-level: info
      log-strategy: basic
      aggregate: true
```

The meanings of the four log printing strategies are as follows:

1. `NONE`：No logs.
2. `BASIC`：Logs request and response lines.
3. `HEADERS`：Logs request and response lines and their respective headers.
4. `BODY`：Logs request and response lines and their respective headers and bodies (if present).

#### Declarative Log Printing

If only some requests are required to print the log, you can use the `@Logging` annotation on the relevant interface or method.

#### Log printing custom extension

If you need to modify the log printing behavior, you can inherit `LoggingInterceptor` and configure it as a `Spring bean`.

### Request Retry

Component support supports global retry and declarative retry.

#### Global Retry

Global retry is disabled by default, and the default configuration items are as follows:

```yaml
retrofit:
  global-retry:
     enable: false
     interval-ms: 100
     max-retries: 2
     retry-rules:
        - response_status_not_2xx
        - occur_io_exception
 ```

The retry rule supports three configurations:

1. `RESPONSE_STATUS_NOT_2XX`: retry when response status code is not `2xx`
2. `OCCUR_IO_EXCEPTION`: Execute retry when IO exception occurs
3. `OCCUR_EXCEPTION`: perform a retry on any exception

#### Declarative Retry

If only a part of the request needs to be retried, you can use the `@Retry` annotation on the corresponding interface or method.

#### Request retry custom extension

If you need to modify the request retry behavior, you can inherit `RetryInterceptor` and configure it as a `Spring bean`.

### Fusing Degrade

The circuit breaker degrade is disabled by default, and currently supports both `sentinel` and `resilience4j` implementations.

```yaml
retrofit:
   degrade:
      # Fuse degrade type. The default is none, which means that fuse downgrade is not enabled
      degrade-type: sentinel
```

#### Sentinel

Configure `degrade-type=sentinel` to enable, and then declare the `@SentinelDegrade` annotation on the relevant interface or method.

Remember to manually import `Sentinel` dependencies:

```xml

<dependency>
   <groupId>com.alibaba.csp</groupId>
   <artifactId>sentinel-core</artifactId>
   <version>1.6.3</version>
</dependency>
```

In addition, global `Sentinel` circuit breaker degrade are also supported:

```yaml
retrofit:
  degrade:
    degrade-type: sentinel
    global-sentinel-degrade:
      enable: true
      # Other sentinel global configuration
```

#### Resilience4j

Configure `degrade-type=resilience4j` to enable. Then declare `@Resilience4jDegrade` on the relevant interface or method.

Remember to manually import `Resilience4j` dependencies:

```xml

<dependency>
   <groupId>io.github.resilience4j</groupId>
   <artifactId>resilience4j-circuitbreaker</artifactId>
   <version>1.7.1</version>
</dependency>
```

In addition, global `Resilience4j` circuit breaker degrade are also supported:

```yaml
retrofit:
   degrade:
      degrade-type: resilience4j
      global-resilience4j-degrade:
         enable: true
         # Get CircuitBreakerConfig from {@link CircuitBreakerConfigRegistry} based on this name as a global circuit breaker configuration
         circuit-breaker-config-name: defaultCircuitBreakerConfig
```

Circuit breaker configuration management：

Implement the `CircuitBreakerConfigRegistrar` interface and register the `CircuitBreakerConfig`.

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
   @Override
   public void register(CircuitBreakerConfigRegistry registry) {
   
         registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());
   
         registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                 .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                 .failureRateThreshold(20)
                 .minimumNumberOfCalls(5)
                 .permittedNumberOfCallsInHalfOpenState(5)
                 .build());
   }
}
 ```

Specify the `CircuitBreakerConfig` via `circuitBreakerConfigName`. Include `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` or `@Resilience4jDegrade.circuitBreakerConfigName`


#### Extended circuit breaker degrade

If the user needs to use another circuit breaker degrade implementation, inherit `BaseRetrofitDegrade` and configure it with `Spring Bean`.

#### Configure fallback or fallbackFactory (optional)

If `@RetrofitClient` does not set `fallback` or `fallbackFactory`, when a circuit breaker is triggered, a `RetrofitBlockException` exception will be thrown directly. Users can customize the method return value when blown by setting `fallback` or `fallbackFactory`.

> Note: `fallback` class must be the implementation class of the current interface, `fallbackFactory` must be `FallbackFactory<T>`
Implementation class, the generic parameter type is the current interface type. In addition, `fallback` and `fallbackFactory` instances must be configured as `Spring Bean`.

The main difference between `fallbackFactory` and `fallback` is that it can perceive the abnormal cause (cause) of each fuse. The reference example is as follows:

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
      };
   }
}
```

### Error Decoder

When a request error occurs in `HTTP` (including an exception or the response data does not meet expectations), the error decoder can decode the `HTTP` related information into a custom exception. You can use `errorDecoder()` in the `@RetrofitClient` annotation
Specifies the error decoder of the current interface. Custom error decoders need to implement the `ErrorDecoder` interface:


### HTTP Calls Between Microservices

#### Inherit `ServiceInstanceChooser`

Users can implement the `ServiceInstanceChooser` interface by themselves, complete the selection logic of service instances, and configure them as `Spring Bean`. For `Spring Cloud`
Application, it can be implemented using the following.

```java
@Service
public class SpringCloudServiceInstanceChooser implements ServiceInstanceChooser {

   private LoadBalancerClient loadBalancerClient;

   @Autowired
   public SpringCloudServiceInstanceChooser(LoadBalancerClient loadBalancerClient) {
      this.loadBalancerClient = loadBalancerClient;
   }

   /**
    * Chooses a ServiceInstance URI from the LoadBalancer for the specified service.
    *
    * @param serviceId The service ID to look up the LoadBalancer.
    * @return Return the uri of ServiceInstance
    */
   @Override
   public URI choose(String serviceId) {
      ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
      Assert.notNull(serviceInstance, "can not found service instance! serviceId=" + serviceId);
      return serviceInstance.getUri();
   }
}
```

#### Specify `serviceId` and `path`

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

   /**
    * 根据id查询用户信息
    */
   @GET("getUser")
   User getUser(@Query("id") Long id);
}
```

## Global Interceptor

### Global Application Interceptor

If we need to perform unified interception processing for `HTTP` requests of the entire system, we can implement the global interceptor `GlobalInterceptor` and configure it as `spring Bean`.

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
   @Override
   public Response intercept(Chain chain) throws IOException {
      Response response = chain.proceed(chain.request());
      // response的Header加上global
      return response.newBuilder().header("global", "true").build();
   }
}
```

### Global Network Interceptor

Implement the `NetworkInterceptor` interface and configure it as a `spring Bean`.

## Call Adapter

`Retrofit` can adapt `Call<T>` objects to the return type of interface methods through `CallAdapterFactory`. The component extends some `CallAdapterFactory` implementations:

1. `BodyCallAdapterFactory`
    - Execute the `HTTP` request synchronously, adapting the content of the response body to the return value type of the method.
    - Any method return value type can use `BodyCallAdapterFactory`, with the lowest priority.
2. `ResponseCallAdapterFactory`
    - Execute the `HTTP` request synchronously, adapt the content of the response body to `Retrofit.Response<T>` and return it.
    - The `ResponseCallAdapterFactory` can only be used if the method return value type is `Retrofit.Response<T>`.
3. Reactive programming related `CallAdapterFactory`, supports the following method return value types:
   
**`Retrofit` will select the corresponding `CallAdapterFactory` to perform adaptation processing according to the return value type of the method**. The currently supported return value types are as follows:

- String：Adapt `Response Body` to `String` to return.
- Basic type (`Long`/`Integer`/`Boolean`/`Float`/`Double`): adapt `Response Body` to the above basic type
- Any `Java` type: adapt the `Response Body` to the corresponding `Java` object and return it
- `CompletableFuture<T>`: adapt `Response Body` to a `CompletableFuture<T>` object and return it
- `Void`: `Void` can be used regardless of the return type
- `Response<T>`: `Response<T>`: adapt `Response` to a `Response<T>` object and return it
- `Call<T>`: No adaptation processing is performed, and the `Call<T>` object is returned directly
- `Mono<T>`: `Project Reactor` reactive return type
- `Single<T>`: `Rxjava` reactive return type (supports `Rxjava2/Rxjava3`)
- `Completable`: `Rxjava` reactive return type, `HTTP` request has no response body (supports `Rxjava2/Rxjava3`)


`CallAdapter` can be extended by extending `CallAdapter.Factory`.

Components support configuring global call adapter factories via `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # The `CallAdaptorFactory` factory extended by the component has been built in, please do not repeat the configuration here
  global-call-adapter-factories:
    # ...
```

For each Java interface, you can also specify the `CallAdapter.Factory` used by the current interface through `@RetrofitClient.callAdapterFactories`.

> Recommendation: configure `CallAdapter.Factory` as `Spring Bean`

### Data Converter

`Retrofit` uses `Converter` to convert the object annotated with `@Body` into `Request Body`, and `Response Body` into a `Java` object. You can choose the following `Converter`:

- [Gson](https://github.com/google/gson): com.squareup.Retrofit:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.Retrofit:converter-jackson
- [Moshi](https://github.com/square/moshi/): com.squareup.Retrofit:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.Retrofit:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.Retrofit:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.Retrofit:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- fastJson：com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

Configure the global `Converter.Factory` through `retrofit.global-converter-factories`, the default is `retrofit2.converter.jackson.JacksonConverterFactory`.

If you need to modify the `Jackson` configuration, you can override the `bean` configuration of the `JacksonConverterFactory` by yourself.

```yaml
retrofit:
   global-converter-factories:
      - com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory
      - retrofit2.converter.jackson.JacksonConverterFactory
```

For each `Java` interface, you can also specify the `Converter.Factory` used by the current interface through `@RetrofitClient.converterFactories`.

> Recommendation: Configure `Converter.Factory` as `Spring Bean`.

### Meta-annotation

Annotations such as `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` support meta-annotations, inheritance, and `@AliasFor`.

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Logging(logLevel = LogLevel.WARN)
@Retry(intervalMs = 200)
public @interface MyRetrofitClient {

   @AliasFor(annotation = RetrofitClient.class, attribute = "converterFactories")
   Class<? extends Converter.Factory>[] converterFactories() default {GsonConverterFactory.class};

   @AliasFor(annotation = Logging.class, attribute = "logStrategy")
   LogStrategy logStrategy() default LogStrategy.BODY;
}
```

## Other Examples

### Form Parameter

```java
@FormUrlEncoded
@POST("token/verify")
 Object tokenVerify(@Field("source") String source,@Field("signature") String signature,@Field("token") String token);


@FormUrlEncoded
@POST("message")
CompletableFuture<Object> sendMessage(@FieldMap Map<String, Object> param);
```

### File Upload

#### Create MultipartBody.Part

```java
// 对文件名使用URLEncoder进行编码
public ResponseEntity importTerminology(MultipartFile file){
     String fileName=URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()),"utf-8");
     okhttp3.RequestBody requestBody=okhttp3.RequestBody.create(MediaType.parse("multipart/form-data"),file.getBytes());
     MultipartBody.Part part=MultipartBody.Part.createFormData("file",fileName,requestBody);
     apiService.upload(part);
     return ok().build();
}
```

#### `HTTP` Upload Interface

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

### File Download

#### `HTTP` Download Interface

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}

```

#### `HTTP` Download Using

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
        InputStream is = responseBody.byteStream();

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

### Dynamic URL

Use the `@url` annotation to implement dynamic URLs. At this point, `baseUrl` can be configured with any legal url. For example: `http://github.com/` . The runtime will only make requests based on the `@Url` address.

> Note: `@url` must be placed in the first position of the method parameter. In addition, on annotations such as `@GET` and `@POST`, there is no need to define the endpoint path.
> 
```java
 @GET
 Map<String, Object> test3(@Url String url,@Query("name") String name);
```

### `DELETE` request adds request body

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

### `GET` request adds request body

`okhttp3` itself does not support the `GET` request to add a request body, the source code is as follows:

![image](https://user-images.githubusercontent.com/30620547/108949806-0a9f7780-76a0-11eb-9eb4-326d5d546e98.png)

![image](https://user-images.githubusercontent.com/30620547/108949831-1ab75700-76a0-11eb-955c-95d324084580.png)

The author gives the specific reasons, you can refer to: [issue](https://github.com/square/okhttp/issues/3154)

However, if you really need to do this, you can use: `@HTTP(method = "get", path = "/user/get", hasBody = true)`, Use lowercase `get` to bypass the above restrictions.
