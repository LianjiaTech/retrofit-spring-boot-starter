# 熔断降级
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | **繁體中文** | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

熔断降级預設關閉，當前支援 **Sentinel** 和 **Resilience4j** 兩種實作。

```yaml
retrofit:
  degrade:
    # 熔断降级型別，預設 none 表示不啟用
    degrade-type: sentinel
```

## Sentinel

### 引入依賴

手動引入 Sentinel 依賴：

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### 宣告式熔断

配置 `degrade-type=sentinel` 開啟，然后在相關介面或者方法上宣告 `@SentinelDegrade` 注解：

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

### 全域 Sentinel 熔断降级

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # 降级策略（0：平均响应時間；1：例外比例；2：例外數量）
      - grade: 0
        # 各降级策略對應的阈值。平均响应時間(ms)，例外比例(0-1)，例外數量(1-N)
        count: 1000
        # 熔断时长，单位為 s
        time-window: 5
        # （在有效統計時間範圍内）能夠觸發熔断的最小請求數
        min-request-amount: 5
        # RT 模式下慢請求率的阈值
        slow-ratio-threshold: 1.0
        # 時間間隔統計持續時間，单位為毫秒
        stat-interval-ms: 1000
```

## Resilience4j

### 引入依賴

手動引入 Resilience4j 依賴：

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### 注册熔断配置

實作 `CircuitBreakerConfigRegistrar` 介面，注册 `CircuitBreakerConfig`：

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // 替换預設的 CircuitBreakerConfig
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // 注册其他的 CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### 宣告式熔断

配置 `degrade-type=resilience4j` 開啟，然后在相關介面或者方法上宣告 `@Resilience4jDegrade`：

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

### 全域 Resilience4j 熔断降级

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # 根據該名稱從 CircuitBreakerConfigRegistry 取得 CircuitBreakerConfig，作為全域熔断配置
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

透過 `circuitBreakerConfigName` 指定 `CircuitBreakerConfig`，包括 `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` 或者 `@Resilience4jDegrade.circuitBreakerConfigName`。

## 擴展熔断降级

如果需要使用其他熔断降级實作，繼承 `BaseRetrofitDegrade`，並將其配置成 Spring Bean。

## Fallback 与 FallbackFactory

如果 `@RetrofitClient` 不設定 `fallback` 或者 `fallbackFactory`，當觸發熔断时会直接抛出 `RetrofitBlockException` 例外。使用者可以透過設定 `fallback` 或者 `fallbackFactory` 來定製熔断时的方法回傳值。

> 注意：`fallback` 類必須是當前介面的實作類，`fallbackFactory` 必須是 `FallbackFactory<T>` 實作類，泛型參數型別為當前介面型別。另外，`fallback` 和 `fallbackFactory` 實例必須配置成 Spring Bean。

`fallbackFactory` 相對於 `fallback`，主要差別在於能夠感知每次熔断的例外原因（cause）。

### Fallback 示例

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

### FallbackFactory 示例

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("觸發熔断了! ", cause.getMessage(), cause);
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

[上一節：拦截器](interceptor.md) | [下一節：错误解码器](error-decoder.md)