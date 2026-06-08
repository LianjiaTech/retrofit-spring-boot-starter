# 인터셉터
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | **한국어** | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

이 컴포넌트는 4가지 인터셉터 메커니즘을 제공하여, 다양한 시나리오의 HTTP 요청 인터셉트 요구를 충족합니다.

## 글로벌 애플리케션 인터셉터

시스템 전체의 HTTP 요청에 대해 통일적인 인터셉트 처리를 수행해야 하는 경우, `GlobalInterceptor` 인터페이스를 구현하고 Spring Bean으로 설정합니다:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response의 Header에 global 추가
        return response.newBuilder().header("global", "true").build();
    }
}
```

## 글로벌 네트워크 인터셉터

`NetworkInterceptor` 인터페이스를 구현하고 Spring Bean으로 설정합니다.

## 어노테이션식 경로 매칭 인터셉터

다양한 시나리오에서 일부 HTTP 인터페이스만 특별한 로직을 적용해야 하는 경우가 있습니다. 이 경우 경로 매칭 인터셉터를 사용하여 이 기능을优雅하게 구현할 수 있습니다.

### BasePathMatchInterceptor를 상속하여 인터셉트 핸들러 작성

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response의 Header에 path.match 추가
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### 인터페이스에 @Intercept 표시

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 여러 경로 매칭 인터셉터가 필요한 경우, @Intercept를 추가하세요
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

위 `@Intercept` 설정은 `InterceptorUserService` 인터페이스의 `/api/user/**` 경로(`/api/user/getUser` 제외) 요청을 인터셉트하고, 인터셉트 핸들러로 `PathMatchInterceptor`를 사용하는 것을 의미합니다. 여러 인터셉터가 필요한 경우, 인터페이스에 여러 `@Intercept` 어노테이션을 표시하세요.

## 커스텀 인터셉터 어노테이션

인터셉트 어노테이션에 동적으로 매개변수를 전달하고, 인터셉트 시 이 매개변수를 사용해야 하는 경우가 있습니다. 이 경우 "커스텀 인터셉터 어노테이션"을 사용할 수 있습니다. 단계는 다음과 같습니다:

1. 커스텀 어노테이션. `@InterceptMark`로 표시해야 하며, 어노테이션에 `include`, `exclude`, `handler` 필드를 포함해야 합니다
2. `BasePathMatchInterceptor`를 상속하여 인터셉트 핸들러 작성
3. 인터페이스에서 커스텀 어노테이션 사용

다음에 "요청 헤더에 `accessKeyId`, `accessKeySecret` 서명 정보를 동적으로 추가"하는 예로 전체 흐름을 시연합니다.

### 커스텀 @Sign 어노테이션

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

`@Sign` 어노테이션에서 사용하는 인터셉터로 `SignInterceptor`를指定했습니다.

### SignInterceptor 구현

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

> 주의: `accessKeyId`와 `accessKeySecret` 필드에는 `setter` 메서드를 제공해야 합니다.

인터셉터의 `accessKeyId`와 `accessKeySecret` 필드值은 `@Sign` 어노테이션의 `accessKeyId()`와 `accessKeySecret()` 값에 기반하여 자동으로 주입됩니다. `@Sign`에서 플레이스홀더 형식의 문자열이 지정된 경우, 설정 프로퍼티 값이取得되어 주입됩니다.

### 인터페이스에서 @Sign 사용

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[이전 섹션: 요청 재시도](retry.md) | [다음 섹션: 서킷브레이커 / 데그레이드](degrade.md)