# サーキットブレーカー/フェイルバック
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | **日本語** | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

サーキットブレーカー/フェイルバックはデフォルトで無効です。現在 **Sentinel** と **Resilience4j** の2つの実装をサポートしています。

```yaml
retrofit:
  degrade:
    # サーキットブレーカー/フェイルバック型、デフォルト none は無効を意味
    degrade-type: sentinel
```

## Sentinel

### 依存関係の追加

Sentinel 依存関係を手動で追加します：

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### 宣言型サーキットブレーカー

`degrade-type=sentinel` で有効化し、関連インターフェースまたはメソッドに `@SentinelDegrade` アノテーションを宣言します：

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = SentinelFallbackUserService.class)
@SentinelDegrade(rules = {
    @SentinelDegradeRule(grade = 0, count = 100, timeWindow = 4),
    @SentinelDegradeRule(grade = 1, count = 0.01, timeWindow = 3)
})
public interface SentinelUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @SentinelDegrade(rules = {@SentinelDegradeRule(grade = 2, count = 1, timeWindow = 6)})
    User getUser(@Query("id") Long id);
}
```

### グローバル Sentinel サーキットブレーカー/フェイルバック

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # フェイルバックストラテジー（0：平均レスポンス時間；1：例外比率；2：例外数）
      - grade: 0
        # 各フェイルバックストラテジーに対応する閾値。平均レスポンス時間(ms)、例外比率(0-1)、例外数(1-N)
        count: 1000
        # サーキットブレーカー時間、単位 s
        time-window: 5
        # （有効統計時間範囲内）サーキットブレーカーをトリガーできる最小リクエスト数
        min-request-amount: 5
        # RT モードでの低速リクエスト率閾値
        slow-ratio-threshold: 1.0
        # 統計区間持続時間、単位ミリ秒
        stat-interval-ms: 1000
```

## Resilience4j

### 依存関係の追加

Resilience4j 依存関係を手動で追加します：

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### サーキットブレーカー設定の登録

`CircuitBreakerConfigRegistrar` インターフェースを実装し、`CircuitBreakerConfig` を登録します：

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // デフォルトの CircuitBreakerConfig を置換
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // 他の CircuitBreakerConfig を登録
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### 宣言型サーキットブレーカー

`degrade-type=resilience4j` で有効化し、関連インターフェースまたはメソッドに `@Resilience4jDegrade` を宣言します：

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = Resilience4jFallbackFactory.class)
@Resilience4jDegrade(circuitBreakerConfigName = "testCircuitBreakerConfig")
public interface Resilience4jUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @Resilience4jDegrade(enable = false)
    User getUser(@Query("id") Long id);
}
```

### グローバル Resilience4j サーキットブレーカー/フェイルバック

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # この名前で CircuitBreakerConfigRegistry から CircuitBreakerConfig を取得し、グローバルサーキットブレーカー設定として使用
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

`circuitBreakerConfigName` で `CircuitBreakerConfig` を指定します。`retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` または `@Resilience4jDegrade.circuitBreakerConfigName` が利用可能です。

## サーキットブレーカー/フェイルバックの拡張

他のサーキットブレーカー/フェイルバック実装を使用する必要がある場合は、`BaseRetrofitDegrade` を継承し、Spring Bean として設定します。

## Fallback と FallbackFactory

`@RetrofitClient` に `fallback` または `fallbackFactory` を設定しない場合、サーキットブレーカーがトリガーされた時は `RetrofitBlockException` 例外が直接スローされます。ユーザーは `fallback` または `fallbackFactory` を設定して、サーキットブレーカー時のメソッド戻り値をカスタマイズできます。

> 注意：`fallback` クラスは現在のインターフェースの実装クラスである必要があり、`fallbackFactory` は `FallbackFactory<T>` 実装クラスである必要があります。ジェネリックパラメーター型は現在のインターフェース型です。また、`fallback` と `fallbackFactory` インスタンスは Spring Bean として設定する必要があります。

`fallbackFactory` と `fallback` の主な違いは、サーキットブレーカーの例外原因（cause）を感知できることです。

### Fallback 例

```java
@Slf4j
@Service
public class HttpDegradeFallback implements HttpDegradeApi {

    @Override
    public Result<Integer> test() {
        Result<Integer> fallback = new Result<>();
        fallback.setCode(100)
                .setMsg("fallback")
                .setBody(1000000);
        return fallback;
    }
}
```

### FallbackFactory 例

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("サーキットブレーカーがトリガーされました! ", cause.getMessage(), cause);
        return new HttpDegradeApi() {
            @Override
            public Result<Integer> test() {
                Result<Integer> fallback = new Result<>();
                fallback.setCode(100)
                        .setMsg("fallback")
                        .setBody(1000000);
                return fallback;
            }
        };
    }
}
```

---

[前へ：インターセプター](interceptor.md) | [次へ：エラーデコーダー](error-decoder.md)