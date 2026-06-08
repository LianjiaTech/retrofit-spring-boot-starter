# 自定义 OkHttpClient 与 Call.Factory SPI
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | **繁體中文** | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

本元件為每個 `@RetrofitClient` 介面建立一個配置好的 `OkHttpClient`（含全部拦截器、超时、连接池等），並作為 Retrofit 的 `Call.Factory` 使用。以下介紹兩種定製方式：

## 自定义 OkHttpClient

對於超时相關配置，可以透過配置檔案或 `@Timeout` 注解設定（參見[超时配置](timeout.md)）。但如果需要更靈活複雜的 OkHttpClient 配置，推薦透過自定义 OkHttpClient 實作。

### 實作 SourceOkHttpClientRegistrar 介面

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

### 指定介面使用的 OkHttpClient

透過 `@RetrofitClient.sourceOkHttpClient` 指定當前介面要使用的 OkHttpClient：

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 自定义 Call.Factory SPI

如果需要在 Call 建立層面做自定义（如動態 callTimeout、請求級定製等），可透過 `CallFactoryConfigurer` SPI 實作。

> **為什麼需要 SPI？** OkHttp 的 `callTimeout` 是整個呼叫的截止時間，無法在拦截器中可靠覆蓋（OkHttp 在拦截器鏈執行前已完成 timeout 調度）。`CallFactoryConfigurer` 在 Call 建立層面介入，使用 `OkHttpClient.newBuilder()` 派生輕量 client（共享 connectionPool 与 dispatcher）來實作 per-request 覆蓋。

### 實作 CallFactoryConfigurer 介面

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
                        // newBuilder() 共享 connectionPool/dispatcher/interceptors，僅 callTimeout 不同
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // 無覆蓋 → 使用 @Timeout 或全域預設值
                return baseClient.newCall(request);
            }
        };
    }
}
```

### 僅對特定介面生效

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
        // 其他介面直接回傳 baseClient，等價預設行為
        return baseClient;
    }
}
```

> 未注册 `CallFactoryConfigurer` Bean 时，元件行為完全不變。當 `CallFactoryConfigurer` 回傳的不是 `OkHttpClient` 时，方法級 `@Timeout` 不生效——使用者應在自定义實作中自行處理超时。

---

[上一節：自定义数据转换器](converter.md) | [下一節：方法级超时配置](timeout.md)