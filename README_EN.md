## retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-chentianming-orange.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)
[![QQ-Group](https://img.shields.io/badge/QQ_Group-806714302-orange.svg?style=flat-square) ](https://img.ljcdn.com/hc-picture/6302d742-ebc8-4649-95cf-62ccf57a1add)

[中文文档](https://github.com/LianjiaTech/retrofit-spring-boot-starter/)

[retrofit](https://square.github.io/retrofit/) enables the conversion of HTTP APIs into Java interfaces. This component deeply integrates Retrofit with Spring Boot and supports various practical feature enhancements.

- **For Spring Boot 3.x projects, use retrofit-spring-boot-starter 3.x**
- **For Spring Boot 1.x/2.x projects, use [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)**, which supports Spring Boot 1.4.2 and above.

> 🚀 The project is continuously optimized and iterated. Contributions via ISSUES and PRs are welcome! Please consider giving a star⭐️—your support motivates our ongoing updates!

GitHub project link: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)  
Gitee project link: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)


## Quick Start

### Add Dependency

```xml  
<dependency>  
    <groupId>com.github.lianjiatech</groupId>  
    <artifactId>retrofit-spring-boot-starter</artifactId>  
    <version>3.2.0</version>  
</dependency>  
```  

For most Spring Boot projects, adding the dependency is sufficient. If the component fails to work after dependency injection, try the following solutions:

#### Manual Auto-configuration Import

In some cases, `RetrofitAutoConfiguration` may not load properly. Attempt manual configuration import with the following code:

```java  
@Configuration  
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})  
public class SpringBootAutoConfigBridge {  
}  
```  

If the project still uses Spring XML configuration files, add the Spring Boot auto-configuration class to the XML file:

```xml  
<!-- Import Spring Boot auto-configuration class -->  
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```  


### Define HTTP Java Interface

**Interfaces must be annotated with `@RetrofitClient`!**

```java  
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")  
public interface UserService {  

    /**  
     * Query username by ID  
     */  
    @POST("getName")  
    String getName(@Query("id") Long id);  
}  
```  

> Note: **Avoid using leading slashes (`/`) in method request paths**. For Retrofit, if `baseUrl = http://localhost:8080/api/test/`:
> - A method path `person` results in the full URL: `http://localhost:8080/api/test/person`.
> - A method path `/person` results in the full URL: `http://localhost:8080/person`.


### Injection and Usage

**Inject the interface into other Services for use!**

```java  
@Service  
public class BusinessService {  

    @Autowired  
    private UserService userService;  

    public void doBusiness() {  
        // Call userService methods  
    }  
}  
```  

**By default, `RetrofitClient` interfaces are automatically registered via Spring Boot's component scanning path**. Alternatively, specify a custom scan path using `@RetrofitScan` on a configuration class.


## HTTP Request Annotations

HTTP request-related annotations use Retrofit's native annotations. A brief overview is provided below:

| Annotation Category | Supported Annotations |  
|---------------------|-----------------------|  
| Request Methods     | `@GET`, `@HEAD`, `@POST`, `@PUT`, `@DELETE`, `@OPTIONS`, `@HTTP` |  
| Request Headers     | `@Header`, `@HeaderMap`, `@Headers` |  
| Query Parameters    | `@Query`, `@QueryMap`, `@QueryName` |  
| Path Parameters     | `@Path` |  
| Form-Encoded Params | `@Field`, `@FieldMap`, `@FormUrlEncoded` |  
| Request Body        | `@Body` |  
| File Upload         | `@Multipart`, `@Part`, `@PartMap` |  
| URL Parameters      | `@Url` |  

> For details, refer to the official documentation: [Retrofit Official Documentation](https://square.github.io/retrofit/)


## Feature Highlights

### Automatic Adaptation of HTTP Responses to Java Return Types

This component automatically adapts HTTP responses to the return types defined in Java interfaces. The following return types are supported:

- `Call<T>`: Returns the `Call<T>` object directly without adaptation.
- `String`: Adapts the `Response Body` to a `String`.
- Primitive Types (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Adapts the `Response Body` to the specified primitive type.
- `CompletableFuture<T>`: Adapts the `Response Body` to a `CompletableFuture<T>`.
- `Void`: Use for requests where the return type is irrelevant.
- `Response<T>`: Adapts the response to a `Response<T>` object.
- `Mono<T>`: Reactive return type for Project Reactor.
- `Single<T>`: Reactive return type for RxJava (supports RxJava2/RxJava3).
- `Completable`: RxJava reactive return type for HTTP requests with no response body (supports RxJava2/RxJava3).
- Custom POJOs: Adapts the `Response Body` to the specified POJO.


#### Adaptation Implementation

Retrofit uses `CallAdapterFactory` to adapt `Call<T>` objects to method return types. This component extends `CallAdapterFactory` with the following implementations:

- `BodyCallAdapterFactory`:
    - Executes HTTP requests synchronously and adapts the response body to the method's return type.
    - Supports all return types with the lowest priority.

- `ResponseCallAdapterFactory`:
    - Executes HTTP requests synchronously and adapts the response to `Retrofit.Response<T>`.
    - Only applicable when the return type is `Retrofit.Response<T>`.

- Reactive Programming `CallAdapterFactory`:
    - Supports reactive types like `Mono<T>`, `Single<T>`, and `Completable`.

Custom adaptations can be implemented by extending `CallAdapter.Factory`. Configure global call adapter factories via `retrofit.global-call-adapter-factories`:

```yaml  
retrofit:  
  # Global adapter factories (component-extended CallAdapterFactories are pre-included; do not reconfigure here)  
  global-call-adapter-factories:  
    # ...  
```  

For individual interfaces, specify `CallAdapter.Factory` using `@RetrofitClient.callAdapterFactories`.


### Custom Data Converters

Retrofit uses `Converter` to convert `@Body`-annotated objects to HTTP request bodies and response bodies to Java objects. Supported converters include:

- [Gson](https://github.com/google/gson): `com.squareup.Retrofit:converter-gson`
- [Jackson](https://github.com/FasterXML/jackson): `com.squareup.Retrofit:converter-jackson`
- [Moshi](https://github.com/square/moshi/): `com.squareup.Retrofit:converter-moshi`
- [Protobuf](https://developers.google.com/protocol-buffers/): `com.squareup.Retrofit:converter-protobuf`
- [Wire](https://github.com/square/wire): `com.squareup.Retrofit:converter-wire`
- [Simple XML](http://simple.sourceforge.net/): `com.squareup.Retrofit:converter-simplexml`
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): `com.squareup.retrofit2:converter-jaxb`
- FastJson: `com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory`

Configure global `Converter.Factory` via `retrofit.global-converter-factories` (default: `retrofit2.converter.jackson.JacksonConverterFactory`).

To customize Jackson configuration, override the `JacksonConverterFactory` bean:

```yaml  
retrofit:
  # Global converter factories
  global-converter-factories:
    - com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory
    - retrofit2.converter.jackson.JacksonConverterFactory
```  

For individual interfaces, specify `Converter.Factory` using `@RetrofitClient.converterFactories`.


### Custom OkHttpClient

Timeout configurations for `OkHttpClient` can be set via the configuration file or `@RetrofitClient`. For advanced configurations, customize `OkHttpClient` as follows:

#### Implement `SourceOkHttpClientRegistrar`

```java  
@Component  
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {  

    @Override  
    public void register(SourceOkHttpClientRegistry registry) {  
        // Register customOkHttpClient with 1-second timeouts  
        registry.register("customOkHttpClient", new OkHttpClient.Builder()  
                .connectTimeout(Duration.ofSeconds(1))  
                .writeTimeout(Duration.ofSeconds(1))  
                .readTimeout(Duration.ofSeconds(1))  
                .addInterceptor(chain -> chain.proceed(chain.request()))  
                .build());  
    }  
}  
```  

#### Specify `OkHttpClient` in `@RetrofitClient`

```java  
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")  
public interface CustomOkHttpUserService {  

    /**  
     * Query user info by ID  
     */  
    @GET("getUser")  
    User getUser(@Query("id") Long id);  
}  
```  


### Logging

The component supports global and declarative logging.

#### Global Logging

Global logging is enabled by default with the following configuration:

```yaml
retrofit:
  # Global logging configuration
  global-log:
    # Enable logging
    enable: true
    # Global log level
    log-level: info
    # Global log strategy
    log-strategy: basic
    # Aggregate request logs
    aggregate: true
    # Logger name (default: fully qualified class name of LoggingInterceptor)
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    redact-headers: 
```

Logging strategies:

1. `NONE`: No logs.
2. `BASIC`: Logs request and response lines.
3. `HEADERS`: Logs request/response lines and headers.
4. `BODY`: Logs request/response lines, headers, and bodies (if present).

#### Declarative Logging

Use the `@Logging` annotation on interfaces or methods to enable logging for specific requests.

#### Custom Logging Extensions

Extend `LoggingInterceptor` and register it as a Spring bean to customize logging behavior.


### Request Retries

The component supports global and declarative retries.

#### Global Retries

Global retries are disabled by default:

```yaml  
retrofit:
  # Global retry configuration
  global-retry:
    # Enable global retries
    enable: false
    # Retry interval (ms)
    interval-ms: 100
    # Max retries
    max-retries: 2
    # Retry rules
    retry-rules:  
      - response_status_not_2xx
      - occur_io_exception
```  

Retry rules:

1. `RESPONSE_STATUS_NOT_2XX`: Retry on non-2xx status codes.
2. `OCCUR_IO_EXCEPTION`: Retry on IO exceptions.
3. `OCCUR_EXCEPTION`: Retry on any exception.

#### Declarative Retries

Use the `@Retry` annotation on interfaces or methods for selective retries.

#### Custom Retry Extensions

Extend `RetryInterceptor` and register it as a Spring bean to customize retry behavior.


### Global Application Interceptors

Implement `GlobalInterceptor` and register it as a Spring bean to apply unified request processing:

```java  
@Component  
public class MyGlobalInterceptor implements GlobalInterceptor {  
    @Override  
    public Response intercept(Chain chain) throws IOException {  
        Response response = chain.proceed(chain.request());  
        // Add "global" header to responses  
        return response.newBuilder().header("global", "true").build();  
    }  
}  
```  


### Global Network Interceptors

Implement `NetworkInterceptor` and register it as a Spring bean.


### URL Path Matching Interceptors

Use path-matching interceptors to apply special logic to specific HTTP endpoints:

#### Extend `BasePathMatchInterceptor`

```java  
@Component  
public class PathMatchInterceptor extends BasePathMatchInterceptor {  
    @Override  
    protected Response doIntercept(Chain chain) throws IOException {  
        Response response = chain.proceed(chain.request());  
        // Add "path.match" header to responses  
        return response.newBuilder().header("path.match", "true").build();  
    }  
}  
```  

#### Annotate Interfaces with `@Intercept`

```java  
@RetrofitClient(baseUrl = "${test.baseUrl}")  
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")  
// Add multiple @Intercept annotations for multiple interceptors  
public interface InterceptorUserService {  

    /**  
     * Query username by ID  
     */  
    @POST("getName")  
    Response<String> getName(@Query("id") Long id);  

    /**  
     * Query user info by ID  
     */  
    @GET("getUser")  
    Response<User> getUser(@Query("id") Long id);  
}  
```  

The `@Intercept` annotation above applies `PathMatchInterceptor` to requests under `/api/user/**` (excluding `/api/user/getUser`).


### Custom Interceptor Annotations

Define custom annotations to inject dynamic parameters into interceptors:

1. Create an annotation marked with `@InterceptMark`, including `include`, `exclude`, and `handler` fields.
2. Extend `BasePathMatchInterceptor` to implement the interceptor.
3. Apply the custom annotation to interfaces.

**Example: Add dynamic `accessKeyId` and `accessKeySecret` headers**

#### Define `@Sign` Annotation

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
        return response.newBuilder()  
                .addHeader("accessKeyId", accessKeyId)  
                .addHeader("accessKeySecret", accessKeySecret)  
                .build();  
    }  
}  
```  

> Note: Provide setter methods for `accessKeyId` and `accessKeySecret`.

#### Apply `@Sign` to Interfaces

```java  
@RetrofitClient(baseUrl = "${test.baseUrl}")  
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")  
public interface InterceptorUserService {  

    /**  
     * Query all users  
     */  
    @GET("getAll")  
    Response<List<User>> getAll();  
}  
```  


### Circuit Breaking

Circuit breaking is disabled by default, with support for `sentinel` and `resilience4j`:

```yaml  
retrofit:
  # Circuit breaking configuration
  degrade:
    # Circuit breaker type (default: none)
    degrade-type: sentinel
```  

#### Sentinel

Set `degrade-type: sentinel` and use `@SentinelDegrade` on interfaces/methods. Add the Sentinel dependency:

```xml  
<dependency>  
    <groupId>com.alibaba.csp</groupId>  
    <artifactId>sentinel-core</artifactId>  
    <version>1.6.3</version>  
</dependency>  
```  

Enable global Sentinel circuit breaking:

```yaml
retrofit:
  degrade:
    degrade-type: sentinel
    global-sentinel-degrade:
      enable: true
      # ... other Sentinel configurations
```  

#### Resilience4j

Set `degrade-type: resilience4j` and use `@Resilience4jDegrade` on interfaces/methods. Add the Resilience4j dependency:

```xml
<dependency>  
    <groupId>io.github.resilience4j</groupId>  
    <artifactId>resilience4j-circuitbreaker</artifactId>  
    <version>1.7.1</version>  
</dependency>
```  

Enable global Resilience4j circuit breaking:

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

**Circuit Breaker Configuration Management**:

Implement `CircuitBreakerConfigRegistrar` to register `CircuitBreakerConfig`:

```java  
@Component  
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {  
    @Override  
    public void register(CircuitBreakerConfigRegistry registry) {  
        // Override default CircuitBreakerConfig  
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());  

        // Register custom configurations  
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()  
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)  
                .failureRateThreshold(20)  
                .minimumNumberOfCalls(5)  
                .permittedNumberOfCallsInHalfOpenState(5)  
                .build());  
    }  
}  
```  

Specify `CircuitBreakerConfig` via `circuitBreakerConfigName` in global settings or `@Resilience4jDegrade`.


### Error Decoding

Customize error handling by implementing `ErrorDecoder` and specifying it via `@RetrofitClient.errorDecoder()`. Disable with `retrofit.enable-error-decoder=false`.


### HTTP Calls Between Microservices

#### Extend `ServiceInstanceChooser`

Implement `ServiceInstanceChooser` to resolve service instances (example for Spring Cloud):

```java  
@Service  
public class SpringCloudServiceInstanceChooser implements ServiceInstanceChooser {  

    private final LoadBalancerClient loadBalancerClient;  

    @Autowired  
    public SpringCloudServiceInstanceChooser(LoadBalancerClient loadBalancerClient) {  
        this.loadBalancerClient = loadBalancerClient;  
    }  

    @Override  
    public URI choose(String serviceId) {  
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);  
        Assert.notNull(serviceInstance, "Service instance not found! serviceId=" + serviceId);  
        return serviceInstance.getUri();  
    }  
}  
```  

#### Specify `serviceId` and `path`

```java  
@RetrofitClient(serviceId = "user", path = "/api/user")  
public interface ChooserOkHttpUserService {  

    /**  
     * Query user info by ID  
     */  
    @GET("getUser")  
    User getUser(@Query("id") Long id);  
}  
```  


### Custom `@RetrofitClient` Annotations

Combine multiple annotations into a custom annotation for reusability:

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


## Configuration Properties

Customize component behavior via `application.yml` or `application.properties`:

```yaml  
retrofit:
  # Global converter factories
  global-converter-factories:
    - com.github.lianjiatech.retrofit.spring.boot.core.BasicTypeConverterFactory
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Global logging
  global-log:
    enable: true
    log-level: info
    log-strategy: basic
    aggregate: true

  # Global retries
  global-retry:
    enable: false
    interval-ms: 100
    max-retries: 2
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # Global timeouts (ms)  
  global-timeout:
    read-timeout-ms: 10000
    write-timeout-ms: 10000
    connect-timeout-ms: 10000
    call-timeout-ms: 0

  # Circuit breaking  
  degrade:
    degrade-type: none
    global-sentinel-degrade:
      enable: false
      count: 1000
      time-window: 5
      grade: 0
    global-resilience4j-degrade:
      enable: false
      circuit-breaker-config-name: defaultCircuitBreakerConfig
  auto-set-prototype-scope-for-path-math-interceptor: true
  enable-error-decoder: true
```  

**Manual `RetrofitProperties` Configuration** (if YAML properties fail):

```java  
@Bean  
public RetrofitProperties retrofitProperties() {  
    RetrofitProperties properties = new RetrofitProperties();  
    // Customize properties  
    return properties;  
}  
```  


## Additional Examples

### Form Parameters

```java  
@FormUrlEncoded  
@POST("token/verify")  
Object tokenVerify(@Field("source") String source, @Field("signature") String signature, @Field("token") String token);  

@FormUrlEncoded  
@POST("message")  
CompletableFuture<Object> sendMessage(@FieldMap Map<String, Object> param);  
```  


### File Upload

#### Create `MultipartBody.Part`

```java  
public ResponseEntity importTerminology(MultipartFile file) {  
    String fileName = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "UTF-8");  
    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file.getBytes());  
    MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);  
    apiService.upload(part);  
    return ResponseEntity.ok().build();  
}  
```  

#### Upload Interface

```java  
@POST("upload")  
@Multipart  
Void upload(@Part MultipartBody.Part file);  
```  


### File Download

#### Download Interface

```java  
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")  
public interface DownloadApi {  

    @GET("{fileKey}")  
    Response<ResponseBody> download(@Path("fileKey") String fileKey);  
}  
```  

#### Usage Example

```java  
@SpringBootTest(classes = {RetrofitBootApplication.class})  
@RunWith(SpringRunner.class)  
public class DownloadTest {  

    @Autowired  
    private DownloadApi downloadApi;  

    @Test  
    public void download() throws Exception {  
        String fileKey = "6302d742-ebc8-4649-95cf-62ccf57a1add";  
        Response<ResponseBody> response = downloadApi.download(fileKey);  
        ResponseBody body = response.body();  

        // Save to file  
        File tempDir = new File("temp");  
        if (!tempDir.exists()) tempDir.mkdir();  
        File file = new File(tempDir, UUID.randomUUID().toString());  
        if (!file.exists()) file.createNewFile();  

        try (InputStream is = body.byteStream();  
             FileOutputStream fos = new FileOutputStream(file)) {  
            byte[] buffer = new byte[1024];  
            int length;  
            while ((length = is.read(buffer)) > 0) {  
                fos.write(buffer, 0, length);  
            }  
        }  
    }  
}  
```  


### Dynamic URLs

Use `@Url` for dynamic endpoints (ignore `baseUrl`):

```java  
@GET  
Map<String, Object> test3(@Url String url, @Query("name") String name);  
```  


### `DELETE` with Request Body

```java  
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)  
```  


### `GET` with Request Body

OkHttp natively blocks `GET` requests with bodies. Bypass using lowercase `method = "get"`:

```java  
@HTTP(method = "get", path = "/user/get", hasBody = true)  
```  

> See: [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)


## Feedback

For issues or suggestions, submit an ISSUE or join the QQ group.