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

**`retrofit-spring-boot-starter` realizes the rapid integration of `Retrofit` and `spring-boot` framework, and supports many functional enhancements, which greatly simplifies development**.


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
   <version>2.3.7</version>
</dependency>
```

### Define HTTP Interface

**Interfaces must be marked with the `@RetrofitClient` annotation**！For HTTP related annotations, please refer to the official documentation：[Retrofit official documentation](https://square.github.io/retrofit/).

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
```

> Notice：**The method request path should be cautiously used at the beginning of `/`**. For `Retrofit`, if `baseUrl=http://localhost:8080/api/test/` and the method request path is `person`, then the complete request path of the method is: `http://localhost: 8080/api/test/person`. If the method request path is `/person`, the complete request path of the method is: `http://localhost:8080/person`.



### Inject Using

**Inject the interface into other services to use**:

```java
@Service
public class TestService {

    @Autowired
    private HttpApi httpApi;

    public void test() {
        // Use `httpApi` to initiate HTTP requests
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

   global-retry:
      enable: false
      interval-ms: 100
      max-retries: 2
      retry-rules:
         - response_status_not_2xx
         - occur_io_exception

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
```

## Advanced Features

### Customize OkHttpClient

1. Implement the `SourceOkHttpClientRegistrar` interface and call the `SourceOkHttpClientRegistry#register()` method to register the `OkHttpClient`.

   ```java
   @Slf4j
   @Component
   public class CustomSourceOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {
   
       @Override
       public void register(SourceOkHttpClientRegistry registry) {
   
           // replace default SourceOkHttpClient. Can be used to modify global `Okhttp Client` settings
           registry.register(Constants.DEFAULT_SOURCE_OK_HTTP_CLIENT, new OkHttpClient.Builder()
                   .connectTimeout(Duration.ofSeconds(5))
                   .writeTimeout(Duration.ofSeconds(5))
                   .readTimeout(Duration.ofSeconds(5))
                   .addInterceptor(chain -> {
                       log.info("============replace default SourceOkHttpClient=============");
                       return chain.proceed(chain.request());
                   })
                   .build());
   
           // add testSourceOkHttpClient
           registry.register("testSourceOkHttpClient", new OkHttpClient.Builder()
                   .connectTimeout(Duration.ofSeconds(3))
                   .writeTimeout(Duration.ofSeconds(3))
                   .readTimeout(Duration.ofSeconds(3))
                   .addInterceptor(chain -> {
                       log.info("============use testSourceOkHttpClient=============");
                       return chain.proceed(chain.request());
                   })
                   .build());
       }
   }
   ```

2. Specify the `OkHttpClient` to be used by the current interface through `@RetrofitClient.sourceOkHttpClient`.

   ```java
   @RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "testSourceOkHttpClient")
   public interface CustomOkHttpTestApi {
   
       @GET("person")
       Result<Person> getPerson(@Query("id") Long id);
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

#### Use the `@Intercept` annotation to specify the interceptor to use

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = TimeStampInterceptor.class, include = {"/api/**"}, exclude = "/api/test/savePerson")
@Intercept(handler = TimeStamp2Interceptor.class) // Need more than one, just add it directly
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

    @POST("savePerson")
    Result<Person> savePerson(@Body Person person);
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

> Note: The `accessKeyId` and `accessKeySecret` fields must provide a `setter` method.

The `accessKeyId` and `accessKeySecret` field values of the interceptor will be automatically injected according to the `accessKeyId()` and `accessKeySecret()` values of the `@Sign` annotation, if `@Sign` specifies a string in the form of a placeholder , the configuration property value will be taken for injection.

#### Using `@Sign` on the interface

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

#### Aggregate log printing

If the logs of the same request need to be aggregated and printed together, `AggregateLoggingInterceptor` can be configured.

```java
@Bean
public LoggingInterceptor loggingInterceptor(RetrofitProperties retrofitProperties){
    return new AggregateLoggingInterceptor(retrofitProperties.getGlobalLog());
}
```

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

1. Implement the `CircuitBreakerConfigRegistrar` interface and register the `CircuitBreakerConfig`.

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

2. Specify the `CircuitBreakerConfig` via `circuitBreakerConfigName`. Include `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` or `@Resilience4jDegrade.circuitBreakerConfigName`


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
Application, component provides `SpringCloudServiceInstanceChooser` implementation, users only need to configure it as `Spring Bean`.

```java
@Bean
@Autowired
public ServiceInstanceChooser serviceInstanceChooser(LoadBalancerClient loadBalancerClient) {
    return new SpringCloudServiceInstanceChooser(loadBalancerClient);
}
```

#### Specify `serviceId` and `path`

```java

@RetrofitClient(serviceId = "${jy-helicarrier-api.serviceId}", path = "/m/count", errorDecoder = HelicarrierErrorDecoder.class)
public interface ApiCountService {}
```

## Global Interceptor

### Global Application Interceptor

If we need to perform unified interception processing for `HTTP` requests of the entire system, we can implement the global interceptor `GlobalInterceptor` and configure it as `spring Bean`.

```java
@Component
public class SourceGlobalInterceptor implements GlobalInterceptor {

   @Autowired
   private TestService testService;

   @Override
   public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      Request newReq = request.newBuilder()
              .addHeader("source", "test")
              .build();
      testService.test();
      return chain.proceed(newReq);
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

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface HttpApi {

   @POST("getString")
   String getString(@Body Person person);

   @GET("person")
   Result<Person> getPerson(@Query("id") Long id);

   @GET("person")
   CompletableFuture<Result<Person>> getPersonCompletableFuture(@Query("id") Long id);

   @POST("savePerson")
   Void savePersonVoid(@Body Person person);

   @GET("person")
   Response<Result<Person>> getPersonResponse(@Query("id") Long id);

   @GET("person")
   Call<Result<Person>> getPersonCall(@Query("id") Long id);

   @GET("person")
   Mono<Result<Person>> monoPerson(@Query("id") Long id);
   
   @GET("person")
   Single<Result<Person>> singlePerson(@Query("id") Long id);
   
   @GET("ping")
   Completable ping();
}

```

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
