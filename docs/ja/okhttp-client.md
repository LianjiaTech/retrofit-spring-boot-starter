# カスタム OkHttpClient と Call.Factory
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | **日本語** | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

本コンポーネントは各 `@RetrofitClient` インターフェースに対して設定済みの `OkHttpClient`（全インターセプター、タイムアウト、接続プール等を含む）を作成し、Retrofit の `Call.Factory` として使用します。以下に2つのカスタマイズ方式を説明します：

## カスタム OkHttpClient

タイムアウト関連設定は設定ファイルまたは `@Timeout` アノテーションで設定可能です（[タイムアウト設定](timeout.md)を参照）。しかし、より柔軟で複雑な OkHttpClient 設定が必要な場合は、カスタム OkHttpClient 実装による方法を推奨します。

### SourceOkHttpClientRegistrar インターフェースの実装

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // customOkHttpClient を登録、タイムアウト時間を 1s に設定
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### インターフェースで使用する OkHttpClient の指定

`@RetrofitClient.sourceOkHttpClient` で現在のインターフェースで使用する OkHttpClient を指定します：

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## カスタム Call.Factory SPI

Call 作成レベルでカスタマイズが必要な場合（動的 callTimeout、リクエストレベルのカスタマイズ等）は、`CallFactoryConfigurer` SPI を実装します。

> **なぜ SPI が必要か？** OkHttp の `callTimeout` は呼び出し全体の制限時間であり、インターセプターで確実にオーバーライドできません（OkHttp はインターセプターチェーン実行前にタイムアウトスケジューリングを完了しています）。`CallFactoryConfigurer` は Call 作成レベルで介入し、`OkHttpClient.newBuilder()` で軽量 client を派生（connectionPool と dispatcher を共有）して per-request オーバーライドを実現します。

### CallFactoryConfigurer インターフェースの実装

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
                        // newBuilder() は connectionPool/dispatcher/interceptors を共有、callTimeoutのみ異なる
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // オーバーライドなし → @Timeout またはグローバルデフォルト値を使用
                return baseClient.newCall(request);
            }
        };
    }
}
```

### 特定インターフェースのみに適用

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
        // 他のインターフェースは baseClient を直接返す、デフォルト動作と同等
        return baseClient;
    }
}
```

> `CallFactoryConfigurer` Bean が未登録の場合、コンポーネントの動作は全く変更されません。

---

[前へ：カスタムデータ変換器](converter.md) | [次へ：メソッドレベルタイムアウト設定](timeout.md)