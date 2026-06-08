# 拦截器
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | **繁體中文** | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

本元件提供四種拦截器機制，滿足不同場景的 HTTP 請求拦截需求。

## 全域應用拦截器

需要對整個系統的 HTTP 請求執行統一拦截處理时，實作 `GlobalInterceptor` 介面，並配置成 Spring Bean：

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

## 全域網路拦截器

實作 `NetworkInterceptor` 介面，並配置成 Spring Bean。

## 注解式路徑匹配拦截器

很多場景下，需要僅針對某些 HTTP 介面做一些特殊邏輯。此时可以使用路徑匹配拦截器，優雅實作該功能。

### 繼承 BasePathMatchInterceptor 编写拦截处理器

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

### 介面上使用 @Intercept 进行标注

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 如果需要使用多個路徑匹配拦截器，繼續添加 @Intercept 即可
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

上面的 `@Intercept` 配置表示：拦截 `InterceptorUserService` 介面下 `/api/user/**` 路徑下（排除 `/api/user/getUser`）的請求，拦截处理器使用 `PathMatchInterceptor`。如果需要使用多個拦截器，在介面上标注多個 `@Intercept` 注解即可。

## 自定义拦截器注解

有时需要在"拦截注解"中動態傳入一些參數，然后在拦截时使用這些參數。此时可以使用"自定义拦截注解"，步驟如下：

1. 自定义注解，必須使用 `@InterceptMark` 标记，并且注解中必須包括 `include`、`exclude`、`handler` 字段
2. 繼承 `BasePathMatchInterceptor` 编写拦截处理器
3. 介面上使用自定义注解

下面以"在請求頭里動態加入 `accessKeyId`、`accessKeySecret` 签名信息"為例，演示完整流程。

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

### 實作 SignInterceptor

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

> 注意：`accessKeyId` 和 `accessKeySecret` 字段必須提供 `setter` 方法。

拦截器的 `accessKeyId` 和 `accessKeySecret` 字段值會依據 `@Sign` 注解的 `accessKeyId()` 和 `accessKeySecret()` 值自動注入。如果 `@Sign` 指定的是佔位符形式的字串，則會取配置屬性值進行注入。

### 介面上使用 @Sign

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[上一節：请求重试](retry.md) | [下一節：熔断降级](degrade.md)