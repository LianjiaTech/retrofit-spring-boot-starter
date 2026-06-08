# OkHttpClient 및 Call.Factory SPI 커스터마이징
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | **한국어** | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

이 컴포넌트는 각 `@RetrofitClient` 인터페이스에 대해 설정된 `OkHttpClient`(모든 인터셉터, 타임아웃, 연결 풀 등 포함)를 생성하고 Retrofit의 `Call.Factory`로 사용합니다. 다음에 두 가지 커스터마이징 방법을 소개합니다:

## OkHttpClient 커스터마이징

타임아웃 관련 설정은 설정 파일이나 `@Timeout` 어노테이션으로 설정할 수 있습니다([타임아웃 설정](timeout.md) 참조). 하지만 더 유연하고 복잡한 OkHttpClient 설정이 필요한 경우, 커스텀 OkHttpClient 구현을 권장합니다.

### SourceOkHttpClientRegistrar 인터페이스 구현

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // customOkHttpClient 등록. 타임아웃 시간을 1s로 설정
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### 인터페이스에서 사용하는 OkHttpClient 지정

`@RetrofitClient.sourceOkHttpClient`로 현재 인터페이스에서 사용할 OkHttpClient를 지정합니다:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Call.Factory SPI 커스터마이징

Call 생성 수준에서 커스터마이징이 필요한 경우(동적 callTimeout, 요청 수준 커스터마이징 등), `CallFactoryConfigurer` SPI로 구현할 수 있습니다.

> **SPI가 필요한 이유**: OkHttp의 `callTimeout`은 전체 호출의 데드라인이며, 인터셉터에서 안정적으로 오버라이드할 수 없습니다(OkHttp는 인터셉터 체인 실행 전에 timeout 스케줄링을 완료합니다). `CallFactoryConfigurer`는 Call 생성 수준에서介入하고, `OkHttpClient.newBuilder()`로 경량 client를 파생(connectionPool과 dispatcher를 공유)하여 per-request 오버라이드를 구현합니다.

### CallFactoryConfigurer 인터페이스 구현

```java
@Component
public class DynamicCallTimeoutConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        return new Call.Factory() {
            @Override
            public Call newCall(Request request) {
                Invocation invocation = request.tag(Invocation.class);
                if (invocation != null) {
                    MyCallTimeout ann = invocation.method().getAnnotation(MyCallTimeout.class);
                    if (ann != null) {
                        // newBuilder()는 connectionPool/dispatcher/interceptors를 공유. callTimeout만 다름
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // 오버라이드 없음 → @Timeout 또는 글로벌 기본값 사용
                return baseClient.newCall(request);
            }
        };
    }
}
```

### 특정 인터페이스만 적용

```java
@Component
public class SelectiveCallFactoryConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        if (retrofitInterface == SlowApiService.class) {
            return baseClient.newBuilder()
                    .callTimeout(30_000, TimeUnit.MILLISECONDS)
                    .build();
        }
        // 다른 인터페이스는 baseClient를 직접 반환. 기본 동작과 동일
        return baseClient;
    }
}
```

> `CallFactoryConfigurer` Bean이 등록되지 않은 경우, 컴포넌트 동작은 완전히 변경되지 않습니다. `CallFactoryConfigurer`가 `OkHttpClient`以外를 반환하는 경우, 메서드 수준 `@Timeout`이 유효하지 않습니다 -- 사용자는 커스텀 구현에서 타임아웃을 자체 처리해야 합니다.

---

[이전 섹션: 데이터 컨버터 커스터마이징](converter.md) | [다음 섹션: 메서드 수준 타임아웃 설정](timeout.md)