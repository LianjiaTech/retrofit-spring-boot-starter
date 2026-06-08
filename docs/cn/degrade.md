# 熔断降级
[English](../en/degrade.md) | **简体中文** | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

熔断降级默认关闭，当前支持 **Sentinel** 和 **Resilience4j** 两种实现。

```yaml
retrofit:
  degrade:
    # 熔断降级类型，默认 none 表示不启用
    degrade-type: sentinel
```

## Sentinel

### 引入依赖

手动引入 Sentinel 依赖：

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### 声明式熔断

配置 `degrade-type=sentinel` 开启，然后在相关接口或者方法上声明 `@SentinelDegrade` 注解：

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

### 全局 Sentinel 熔断降级

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # 降级策略（0：平均响应时间；1：异常比例；2：异常数量）
      - grade: 0
        # 各降级策略对应的阈值。平均响应时间(ms)，异常比例(0-1)，异常数量(1-N)
        count: 1000
        # 熔断时长，单位为 s
        time-window: 5
        # （在有效统计时间范围内）能够触发熔断的最小请求数
        min-request-amount: 5
        # RT 模式下慢请求率的阈值
        slow-ratio-threshold: 1.0
        # 时间间隔统计持续时间，单位为毫秒
        stat-interval-ms: 1000
```

## Resilience4j

### 引入依赖

手动引入 Resilience4j 依赖：

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### 注册熔断配置

实现 `CircuitBreakerConfigRegistrar` 接口，注册 `CircuitBreakerConfig`：

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // 替换默认的 CircuitBreakerConfig
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

### 声明式熔断

配置 `degrade-type=resilience4j` 开启，然后在相关接口或者方法上声明 `@Resilience4jDegrade`：

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

### 全局 Resilience4j 熔断降级

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # 根据该名称从 CircuitBreakerConfigRegistry 获取 CircuitBreakerConfig，作为全局熔断配置
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

通过 `circuitBreakerConfigName` 指定 `CircuitBreakerConfig`，包括 `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` 或者 `@Resilience4jDegrade.circuitBreakerConfigName`。

## 扩展熔断降级

如果需要使用其他熔断降级实现，继承 `BaseRetrofitDegrade`，并将其配置成 Spring Bean。

## Fallback 与 FallbackFactory

如果 `@RetrofitClient` 不设置 `fallback` 或者 `fallbackFactory`，当触发熔断时会直接抛出 `RetrofitBlockException` 异常。用户可以通过设置 `fallback` 或者 `fallbackFactory` 来定制熔断时的方法返回值。

> 注意：`fallback` 类必须是当前接口的实现类，`fallbackFactory` 必须是 `FallbackFactory<T>` 实现类，泛型参数类型为当前接口类型。另外，`fallback` 和 `fallbackFactory` 实例必须配置成 Spring Bean。

`fallbackFactory` 相对于 `fallback`，主要差别在于能够感知每次熔断的异常原因（cause）。

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
        log.error("触发熔断了! ", cause.getMessage(), cause);
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

[上一节：拦截器](interceptor.md) | [下一节：错误解码器](error-decoder.md)