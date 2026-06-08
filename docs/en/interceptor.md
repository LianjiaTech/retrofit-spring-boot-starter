# Interceptors
**English** | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

This component provides four interceptor mechanisms to meet different HTTP request interception needs.

## Global Application Interceptors

When you need to perform unified interception processing for all HTTP requests in the system, implement the `GlobalInterceptor` interface and register it as a Spring Bean:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Add "global" header to response
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Global Network Interceptors

Implement the `NetworkInterceptor` interface and register it as a Spring Bean.

## Annotation-based Path Matching Interceptors

In many scenarios, you only need to apply special logic to certain HTTP interfaces. Path matching interceptors can elegantly implement this functionality.

### Extend BasePathMatchInterceptor to Write an Interceptor Handler

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Add "path.match" header to response
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Annotate Interface with @Intercept

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// If multiple path matching interceptors are needed, just add more @Intercept annotations
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

The `@Intercept` configuration above means: intercept requests under `/api/user/**` path (excluding `/api/user/getUser`) on the `InterceptorUserService` interface, using `PathMatchInterceptor` as the handler. If multiple interceptors are needed, simply add multiple `@Intercept` annotations on the interface.

## Custom Interceptor Annotations

Sometimes you need to dynamically pass parameters in an "interceptor annotation" and use those parameters during interception. You can use "custom interceptor annotations" by following these steps:

1. Create a custom annotation marked with `@InterceptMark`, and the annotation must include `include`, `exclude`, and `handler` fields
2. Extend `BasePathMatchInterceptor` to write an interceptor handler
3. Apply the custom annotation on the interface

Below is an example demonstrating the complete flow using "dynamically adding `accessKeyId` and `accessKeySecret` signature information to request headers".

### Custom @Sign Annotation

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

The `@Sign` annotation specifies `SignInterceptor` as the interceptor to use.

### Implement SignInterceptor

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

> Note: `accessKeyId` and `accessKeySecret` fields must provide setter methods.

The `accessKeyId` and `accessKeySecret` field values of the interceptor will be automatically injected based on the `accessKeyId()` and `accessKeySecret()` values of the `@Sign` annotation. If `@Sign` specifies placeholder-form strings, the configuration property values will be used for injection.

### Apply @Sign on Interface

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[Previous: Request Retries](retry.md) | [Next: Circuit Breaking](degrade.md)