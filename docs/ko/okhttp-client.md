# 커스텀 OkHttpClient와 Call.Factory
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | **한국어** | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

이 컴포넌트는 각 `@RetrofitClient` 인터페이스에 구성된 `OkHttpClient`(전체 인터셉터, 타임아웃, 연결 풀 등 포함)를 생성하고, Retrofit의 `Call.Factory`로 사용합니다. 다음은 두 가지 커스텀 방식을 설명합니다:

## 커스텀 OkHttpClient

타임아웃 관련 설정은 설정 파일이나 `@Timeout` 어노테이션으로 구성할 수 있습니다([타임아웃 설정](timeout.md) 참조). 하지만 더 유연하고 복잡한 OkHttpClient 설정이 필요한 경우, 커스텀 OkHttpClient 구현을 권장합니다.

### SourceOkHttpClientRegistrar 인터페이스 구현

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // customOkHttpClient를 등록, 타임아웃을 1s로 설정
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

`@RetrofitClient.sourceOkHttpClient`를 통해 해당 인터페이스에서 사용할 OkHttpClient를 지정합니다:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 커스텀 Call.Factory SPI

Call 생성 수준에서 커스텀이 필요한 경우(예: 동적 callTimeout, 요청급 커스텀 등), `CallFactoryConfigurer` SPI로 구현할 수 있습니다.

> **SPI가 필요한 이유?** OkHttp의 `callTimeout`은 전체 호출의 마감 시간으로, 인터셉터에서 안정적으로 오버라이드할 수 없습니다(OkHttp는 인터셉터 체인 실행 전에 타임아웃 스케줄링을 완료합니다). `CallFactoryConfigurer`는 Call 생성 수준에서介入하여, `OkHttpClient.newBuilder()`로 lightweight client를 파생(connectionPool과 dispatcher를 공유)하여 per-request 오버라이드를 구현합니다.

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
                        // newBuilder()는 connectionPool/dispatcher/interceptors를 공유, callTimeout만 다름
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // 오버라이드 없음 → @Timeout 또는 전역 기본값 사용
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
        // 다른 인터페이스는 baseClient를 직접 반환, 기본 동작과 동일
        return baseClient;
    }
}
```

> `CallFactoryConfigurer` Bean을 등록하지 않으면 컴포넌트의 동작은 전혀 변하지 않습니다.

---

[이전: 커스텀 데이터 변환기](converter.md) | [다음: 메서드급 타임아웃 설정](timeout.md)