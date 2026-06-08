# 自定义 OkHttpClient 与 Call.Factory
[English](../en/okhttp-client.md) | **简体中文** | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

本组件为每个 `@RetrofitClient` 接口创建一个配置好的 `OkHttpClient`（含全部拦截器、超时、连接池等），并作为 Retrofit 的 `Call.Factory` 使用。以下介绍两种定制方式：

## 自定义 OkHttpClient

对于超时相关配置，可以通过配置文件或 `@Timeout` 注解设置（参见[超时配置](timeout.md)）。但如果需要更灵活复杂的 OkHttpClient 配置，推荐通过自定义 OkHttpClient 实现。

### 实现 SourceOkHttpClientRegistrar 接口

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // 注册 customOkHttpClient，超时时间设置为 1s
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### 指定接口使用的 OkHttpClient

通过 `@RetrofitClient.sourceOkHttpClient` 指定当前接口要使用的 OkHttpClient：

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 自定义 Call.Factory SPI

如果需要在 Call 创建层面做自定义（如动态 callTimeout、请求级定制等），可通过 `CallFactoryConfigurer` SPI 实现。

> **为什么需要 SPI？** OkHttp 的 `callTimeout` 是整个调用的截止时间，无法在拦截器中可靠覆盖（OkHttp 在拦截器链执行前已完成 timeout 调度）。`CallFactoryConfigurer` 在 Call 创建层面介入，使用 `OkHttpClient.newBuilder()` 派生轻量 client（共享 connectionPool 与 dispatcher）来实现 per-request 覆盖。

### 实现 CallFactoryConfigurer 接口

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
                        // newBuilder() 共享 connectionPool/dispatcher/interceptors，仅 callTimeout 不同
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // 无覆盖 → 使用 @Timeout 或全局默认值
                return baseClient.newCall(request);
            }
        };
    }
}
```

### 仅对特定接口生效

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
        // 其他接口直接返回 baseClient，等价默认行为
        return baseClient;
    }
}
```

> 未注册 `CallFactoryConfigurer` Bean 时，组件行为完全不变。

---

[上一节：自定义数据转换器](converter.md) | [下一节：方法级超时配置](timeout.md)