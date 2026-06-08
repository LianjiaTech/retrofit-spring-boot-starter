# 拦截器
[English](../en/interceptor.md) | **简体中文** | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

本组件提供四种拦截器机制，满足不同场景的 HTTP 请求拦截需求。

## 全局应用拦截器

需要对整个系统的 HTTP 请求执行统一拦截处理时，实现 `GlobalInterceptor` 接口，并配置成 Spring Bean：

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response 的 Header 加上 global
        return response.newBuilder().header("global", "true").build();
    }
}
```

## 全局网络拦截器

实现 `NetworkInterceptor` 接口，并配置成 Spring Bean。

## 注解式路径匹配拦截器

很多场景下，需要仅针对某些 HTTP 接口做一些特殊逻辑。此时可以使用路径匹配拦截器，优雅实现该功能。

### 继承 BasePathMatchInterceptor 编写拦截处理器

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response 的 Header 加上 path.match
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### 接口上使用 @Intercept 进行标注

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 如果需要使用多个路径匹配拦截器，继续添加 @Intercept 即可
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

上面的 `@Intercept` 配置表示：拦截 `InterceptorUserService` 接口下 `/api/user/**` 路径下（排除 `/api/user/getUser`）的请求，拦截处理器使用 `PathMatchInterceptor`。如果需要使用多个拦截器，在接口上标注多个 `@Intercept` 注解即可。

## 自定义拦截器注解

有时需要在"拦截注解"中动态传入一些参数，然后在拦截时使用这些参数。此时可以使用"自定义拦截注解"，步骤如下：

1. 自定义注解，必须使用 `@InterceptMark` 标记，并且注解中必须包括 `include`、`exclude`、`handler` 字段
2. 继承 `BasePathMatchInterceptor` 编写拦截处理器
3. 接口上使用自定义注解

下面以"在请求头里动态加入 `accessKeyId`、`accessKeySecret` 签名信息"为例，演示完整流程。

### 自定义 @Sign 注解

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

在 `@Sign` 注解中指定了使用的拦截器是 `SignInterceptor`。

### 实现 SignInterceptor

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

> 注意：`accessKeyId` 和 `accessKeySecret` 字段必须提供 `setter` 方法。

拦截器的 `accessKeyId` 和 `accessKeySecret` 字段值会依据 `@Sign` 注解的 `accessKeyId()` 和 `accessKeySecret()` 值自动注入。如果 `@Sign` 指定的是占位符形式的字符串，则会取配置属性值进行注入。

### 接口上使用 @Sign

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[上一节：请求重试](retry.md) | [下一节：熔断降级](degrade.md)