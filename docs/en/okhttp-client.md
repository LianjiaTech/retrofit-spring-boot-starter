# Custom OkHttpClient and Call.Factory
**English** | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

This component creates a configured `OkHttpClient` (including all interceptors, timeouts, connection pools, etc.) for each `@RetrofitClient` interface and uses it as Retrofit's `Call.Factory`. Two customization approaches are described below:

## Custom OkHttpClient

For timeout-related configuration, you can set it via configuration files or the `@Timeout` annotation (see [Timeout Configuration](timeout.md)). However, if you need more flexible and complex OkHttpClient configuration, a custom OkHttpClient implementation is recommended.

### Implement the SourceOkHttpClientRegistrar Interface

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // Register customOkHttpClient with timeout set to 1s
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### Specify the OkHttpClient for an Interface

Use `@RetrofitClient.sourceOkHttpClient` to specify the OkHttpClient for the current interface:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Custom Call.Factory SPI

If you need to customize at the Call creation level (such as dynamic callTimeout, request-level customization, etc.), you can implement the `CallFactoryConfigurer` SPI.

> **Why is an SPI needed?** OkHttp's `callTimeout` is the deadline for the entire call and cannot be reliably overridden in an interceptor (OkHttp completes timeout scheduling before the interceptor chain executes). `CallFactoryConfigurer` intervenes at the Call creation level, using `OkHttpClient.newBuilder()` to derive a lightweight client (sharing connectionPool and dispatcher) to achieve per-request overrides.

### Implement the CallFactoryConfigurer Interface

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
                        // newBuilder() shares connectionPool/dispatcher/interceptors, only callTimeout differs
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // No override → use @Timeout or global defaults
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Apply Only to Specific Interfaces

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
        // For other interfaces, return baseClient directly, equivalent to default behavior
        return baseClient;
    }
}
```

> When no `CallFactoryConfigurer` Bean is registered, the component behavior remains unchanged.

---

[Previous: Custom Data Converter](converter.md) | [Next: Method-Level Timeout Configuration](timeout.md)