# 인터셉터
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | **한국어** | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

이 컴포넌트는 4가지 인터셉터 메커니즘을 제공하여, 다양한 시나리오의 HTTP 요청 인터셉트需求를 충족합니다.

## 전역 애플리케이션 인터셉터

전체 시스템의 HTTP 요청에 대해 통일된 인터셉트 처리를 실행해야 할 때, `GlobalInterceptor` 인터페이스를 구현하고 Spring Bean으로 구성합니다:

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

## 전역 네트워크 인터셉터

`NetworkInterceptor` 인터페이스를 구현하고 Spring Bean으로 구성합니다.

## 어노테이션 경로 매칭 인터셉터

다양한 시나리오에서, 특정 HTTP 인터페이스에만 특수 로직을 적용해야 할 때가 있습니다. 이때 경로 매칭 인터셉터를 사용하여 해당 기능을优雅하게 구현할 수 있습니다.

### BasePathMatchInterceptor 상속하여 인터셉트 핸들러 작성

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

### 인터페이스에 @Intercept로标注

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 여러 경로 매칭 인터셉터를 사용하려면 @Intercept를 추가하세요
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

위 `@Intercept` 설정의 의미: `InterceptorUserService` 인터페이스下 `/api/user/**` 경로下(`/api/user/getUser` 제외)의 요청을 인터셉트하고, 인터셉트 핸들러로 `PathMatchInterceptor`를 사용합니다. 여러 인터셉터를 사용하려면 인터페이스에 여러 `@Intercept` 어노테이션을标注하면 됩니다.

## 커스텀 인터셉터 어노테이션

"인터셉트 어노테이션"에 동적으로 파라미터를 전달하고, 인터셉트时에 이 파라미터를 사용해야 할 때가 있습니다. 이때 "커스텀 인터셉트 어노테이션"을 사용할 수 있습니다.步骤는 다음과 같습니다:

1. 커스텀 어노테이션을 정의하고, `@InterceptMark`로标记必须하며, 어노테이션에 `include`, `exclude`, `handler` 필드를 포함必须
2. `BasePathMatchInterceptor`를 상속하여 인터셉트 핸들러를 작성
3. 인터페이스에 커스텀 어노테이션을 사용

다음은 "요청 헤더에 `accessKeyId`, `accessKeySecret` 서명 정보를 동적으로 추가" 예시로 전체流程를 시연합니다.

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

`@Sign` 어노테이션에서 사용하는 인터셉터를 `SignInterceptor`로 지정했습니다.

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

> 참고: `accessKeyId`와 `accessKeySecret` 필드에는 `setter` 메서드를 제공必须합니다.

인터셉터의 `accessKeyId`와 `accessKeySecret` 필드 값은 `@Sign` 어노테이션의 `accessKeyId()`와 `accessKeySecret()` 값에 따라 자동으로 주입됩니다. `@Sign`이占位符形式의 문자열을 지정한 경우, 설정 속성 값을 가져와 주입합니다.

### 인터페이스에 @Sign 사용

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[이전: 요청 재시도](retry.md) | [다음: 서킷브레이커/폴백](degrade.md)