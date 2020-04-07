
## 简介

**retrofit-plus是一款基于retrofit2实现的轻量级httpClient客户端工具，与spring和spring-boot项目深度集成。**通过**注解式配置**的方式，可以灵活地配置客户端参数、连接池信息、基于url的路径匹配拦截器、全局拦截器、日志打印策略等。极大地简化了spring(spring-boot)项目中http调用开发。

**github地址:** <https://github.com/LianjiaTech/retrofit-plus>

<!--more-->

> Retrofit2是针对于Android/Java的、基于okHttp的、一种轻量级并使用注解方式和动态代理的网络请求框架。Retrofit2让开发者面向接口去请求服务，使用注解和代理去发起真正的请求，让开发者更快速的开发应用，省掉一些复杂的逻辑处理。

*但是retrofit2官方并没有与spring-boot和spring实现深度整合，而网上各种与spring-boot的整合实现也不尽如人意。因此结合实际的业务场景，基于retrofit2进一步封装实现了retrofit-plus。*

## 特性

- [x] 与spring深度集成
- [x] 与spring-boot深度集成
- [x] http调用接口化
- [x] 连接池管理
- [x] 路径匹配拦截器
- [x] 全局拦截器
- [x] 配置化日志打印

## 要求

**需JDK1.8版本以上**，如不满足请先升级JDK

## 快速使用

以下以spring-boot项目为例，快速使用retrofit-plus！
> 支持spring-boot 1.x/2.x

**与spring集成可参考：[与spring集成](https://github.com/lianjiatech/retrofit-plus/tree/master/doc/spring-integrate.md)**

### 引入依赖

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-plus-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

## 配置`@RetrofitScan`注解

你可以给带有 `@Configuration` 的类配置该注解，或者直接配置到 Spring Boot 的启动类上，如下：

```java
@SpringBootApplication
@RetrofitScan("扫描包路径")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**@RetrofitScan字段含义：[@RetrofitScan](https://github.com/lianjiatech/retrofit-plus/blob/master/retrofit-plus/src/main/java/com/github/lianjiatechtech/retrofit/plus/annotation/RetrofitScan.java)**

### 定义http调用接口

**接口必须使用`@RetrofitClient`注解标记！**

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
```

**@RetrofitClient字段含义：[@RetrofitClient](https://github.com/lianjiatech/retrofit-plus/blob/master/retrofit-plus/src/main/java/com/github/lianjiatech/retrofit/plus/annotation/RetrofitClient.java)**

### 注入使用

**将接口注入到其它bean中即可使用！**

```java
@Autowired
private HttpApi httpApi;

@Test
public void test() {
    Result<Person> person = httpApi.getPerson(1L);
    Person data = person.getData();
    Assert.assertNotNull(data);
    Assert.assertEquals(1L,data.getId().longValue());
    Assert.assertEquals("test",data.getName());
    Assert.assertEquals(10,data.getAge().intValue());
}
```

## 配置一览

| 配置|默认值 | 说明 |
|------------|-----------|--------|
| enable-body-call-adapter | true| 是否启用 BodyCallAdapter适配器 |
| enable-response-call-adapter | true| 是否启用 ResponseCallAdapter适配器 |
| enable-fast-json-converter | true| 是否启用 fast-json 数据转换器 |
| enable-log | true| 启用日志打印 |
| pool | | 连接池配置 |
| disable-void-return-type | false | 禁用java.lang.Void返回类型 |

**配置使用可参考：[配置使用](https://github.com/lianjiatech/retrofit-plus/tree/master/doc/config.md)**

## HTTP请求注解

http请求注解，全部使用了`retrofit`注解。**详细信息可参考官方文档：[retrofit官方文档](https://square.github.io/retrofit/)**

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

## 连接池管理

**你可以在配置文件中配置所需要用到的连接池，在`@RetrofitClient`的使用`poolName`指定所用的连接池！**
*如果没有配置`poolName=default`的连接池，retrofit-plus会按照`max-idle-connections=5`和`keep-alive-second=300`自动配置，当然你也可以自己配置`poolName=default`的连接池以覆盖默认配置值*

### spring-boot项目

#### yml配置

```yml
retrofit-plus:
  # 连接池配置
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
```

#### properties配置

```properties
# 连接池配置
retrofit-plus.pool.test1.max-idle-connections=3
retrofit-plus.pool.test1.keep-alive-second=100
retrofit-plus.pool.test2.max-idle-connections=2
retrofit-plus.pool.test2.keep-alive-second=200
```

### spring项目

#### retrofitHelper配置

```java
// 连接池配置
PoolConfig test1 = new PoolConfig(5, 300);
PoolConfig test2 = new PoolConfig(2, 100);
Map<String, PoolConfig> pool = new HashMap<>();
pool.put("test1", test1);
pool.put("test2", test2);
// 配置对象
Config config = new Config();
config.setPool(pool);
```

## 调用适配器 CallAdapter

Retrofit2可以通过调用适配器`CallAdapterFactory`将`Call<T>`对象适配成接口方法的返回值类型。
retrofit-plus扩展2种`CallAdapterFactory`实现：

1. `BodyCallAdapterFactory`
    - 默认启用，可通过配置`retrofit-plus.enable-body-call-adapter=false`关闭
    - 同步执行http请求，将响应体内容适配成接口方法的返回值类型实例。
    - 如果返回值类型为`retrofit2.Call<T>`、`retrofit2.Response<T>`、`java.util.concurrent.CompletableFuture<T>`，则不会使用适配器。
2. `ResponseCallAdapterFactory`
    - 默认启用，可通过配置`retrofit-plus.enable-response-call-adapter=false`关闭
    - 同步执行http请求，将响应体内容适配成`retrofit2.Response<T>`返回。
    - 如果方法的返回值类型为`retrofit2.Response<T>`，则会使用该适配器。

**retrofit2自动根据方法返回值类型选用对应的`CallAdapterFactory`执行适配处理！加上retrofit2默认的`CallAdapterFactory`，可支持多种形式的方法返回值类型：**

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

**你也可以自己扩展实现自己的`CallAdapter`，只需要继承`CallAdapter.Factory`即可。**

**然后直接将自定义的`CallAdapterFactory`配置成spring的bean即可，retrofit-plus会自动加载！手动配置的`CallAdapterFactory`优先级更高！**

## 数据转码器 Converter

retrofit2使用Converter 将`@Body`注解标注的对象转换成请求体，将响应体数据转换成一个Java对象。你可以选用以下几种Converter：

- Gson: com.squareup.retrofit2:converter-gson
- Jackson: com.squareup.retrofit2:converter-jackson
- Moshi: com.squareup.retrofit2:converter-moshi
- Protobuf: com.squareup.retrofit2:converter-protobuf
- Wire: com.squareup.retrofit2:converter-wire
- Simple XML: com.squareup.retrofit2:converter-simplexml

retrofit-plus默认使用的是fast-json进行序列化转换，你可以通过`retrofit-plus.enable-fast-json-converter=false`关闭该转换器！
**如果需要实现自定义的Converter， 只需继承`Converter.Factory`即可！**

**直接将对应的`ConverterFactory`配置成spring的bean即可，retrofit-plus会自动加载！手动配置的`ConverterFactory`优先级更高！**

## 日志打印配置

针对每个接口，支持日志打印级别和日志打印策略的配置。
**配置使用可参考：[LogStrategy](https://github.com/lianjiatech/retrofit-plus/blob/master/retrofit-plus/src/main/java/com/github/lianjiatech/retrofit/plus/interceptor/LogStrategy.java)**

## 路径匹配拦截器 BasePathMatchInterceptor

可以在接口上使用`@Intercept`注解指定要使用的路径匹配拦截器，参见：[@Intercept](https://github.com/lianjiatech/retrofit-plus/blob/master/retrofit-plus/src/main/java/com/github/lianjiatech/retrofit/plus/annotation/Intercept.java)
> 具体的拦截器需要继承`BasePathMatchInterceptor`

### 示例

给指定请求的url后面拼接timestamp时间戳，可以使用路径匹配拦截器实现

### 拦截器实现

```java
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

### 接口加上`@Intercept`注解（非常实用）

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

**优先从spring容器获取拦截器handler实例，如果获取不到，则使用反射创建一个！** 如果以Bean的形式配置，scope必须是prototype

### 以原型bean的形式配置拦截器实例

适用于**处理逻辑需要依赖其他Bean**的场景

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OtherInterceptor extends BasePathMatchInterceptor {

    @Autowired
    private OtherBean otherBean

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        // 拦截处理
    }
}
```

## 扩展实现自定义拦截注解（非常实用）

如果需要在拦截器注解上传入其它参数，可以通过使用`@InterceptMark`标记来扩展自己的拦截注解。
例如需要给http的request的header中添加sign签名信息，可以扩展一个`@Sign`注解！
> 注意：注解中必须包括`include()、exclude()、handler()`属性信息

### 示例

需要给http的request的header中添加sign签名信息，可以扩展一个`@Sign`注解！

### 定义`@Sign`注解

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
     * 如果以Bean的形式配置，scope必须是prototype
     *
     * @return
     */
    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

### 实现`SignInterceptor`

**自动将注解上的属性注入到拦截器实例的字段上！** 需提供setter方法

```java
@Data
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", resolvePlaceholders(accessKeyId))
                .addHeader("accessKeySecret", resolvePlaceholders(accessKeySecret))
                .build();
        return chain.proceed(newReq);
    }
}
```

### 接口使用

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

## 全局拦截器 BaseGlobalInterceptor

如果你需要对整个系统的的http请求执行统一的拦截处理，可以自定义实现全局拦截器`BaseGlobalInterceptor`, 并配置成spring中的bean即可！

```java
@Component
public class PrintInteceptor extends BaseGlobalInterceptor{
    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        System.out.println("=============test===========");
        return chain.proceed(request);
    }
}
```

## 上传文件示例

### 构建MultipartBody.Part

```java
// 对文件名使用URLEncoder进行编码
String fileName = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "utf-8");
okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse("multipart/form-data"),file.getBytes());
MultipartBody.Part file = MultipartBody.Part.createFormData("file", fileName, requestBody);
apiService.upload(file);
```

### http上传接口

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);

```

## 动态URL示例

使用`@url`注解可实现动态URL。

**注意：`@url`必须放在方法参数的第一个位置。原有定义`@GET`、`@POST`等注解上，不需要定义端点路径！**

```java
 @GET
 Map<String, Object> test3(@Url String url,@Query("name") String name);

```

## 问题反馈

陈添明 <chentianming11@qq.com> ，欢迎Fork&MergeRequest!
