# 攔截器
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | **繁體中文** | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

本元件提供四種攔截器機制，滿足不同場景的 HTTP 請求攔截需求。

## 全域應用攔截器

需要對整個系統的 HTTP 請求執行統一攔截處理時，實作 `GlobalInterceptor` 介面，並設定成 Spring Bean：

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

## 全域網路攔截器

實作 `NetworkInterceptor` 介面，並設定成 Spring Bean。

## 註解式路徑匹配攔截器

很多場景下，需要僅針對某些 HTTP 介面做一些特殊邏輯。此時可以使用路徑匹配攔截器，優雅實作該功能。

### 繼承 BasePathMatchInterceptor 編寫攔截處理器

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

### 介面上使用 @Intercept 進行標註

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 如果需要使用多個路徑匹配攔截器，繼續添加 @Intercept 即可
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

上面的 `@Intercept` 設定表示：攔截 `InterceptorUserService` 介面下 `/api/user/**` 路徑下（排除 `/api/user/getUser`）的請求，攔截處理器使用 `PathMatchInterceptor`。如果需要使用多個攔截器，在介面上標註多個 `@Intercept` 註解即可。

## 自訂攔截器註解

有時需要在「攔截註解」中動態傳入一些參數，然後在攔截時使用這些參數。此時可以使用「自訂攔截註解」，步驟如下：

1. 自訂註解，必須使用 `@InterceptMark` 標記，並且註解中必須包括 `include`、`exclude`、`handler` 欄位
2. 繼承 `BasePathMatchInterceptor` 編寫攔截處理器
3. 介面上使用自訂註解

下面以「在請求頭裡動態加入 `accessKeyId`、`accessKeySecret` 簽名資訊」為例，演示完整流程。

### 自訂 @Sign 註解

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

在 `@Sign` 註解中指定了使用的攔截器是 `SignInterceptor`。

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

> 注意：`accessKeyId` 和 `accessKeySecret` 欄位必須提供 `setter` 方法。

攔截器的 `accessKeyId` 和 `accessKeySecret` 欄位值會依據 `@Sign` 註解的 `accessKeyId()` 和 `accessKeySecret()` 自動注入。如果 `@Sign` 指定的是佔位符形式的字串，則會取設定屬性值進行注入。

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

[上一節：請求重試](retry.md) | [下一節：熔斷降級](degrade.md)