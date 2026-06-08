# Circuit Breaker Degradation
**English** | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

Circuit breaker degradation is disabled by default. Currently, **Sentinel** and **Resilience4j** implementations are supported.

```yaml
retrofit:
  degrade:
    # Circuit breaker degradation type, default none means not enabled
    degrade-type: sentinel
```

## Sentinel

### Add Dependency

Manually add the Sentinel dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### Declarative Circuit Breaking

Configure `degrade-type=sentinel` to enable, then declare the `@SentinelDegrade` annotation on the relevant interface or method:

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

### Global Sentinel Circuit Breaker Degradation

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # Degradation strategy (0: average response time; 1: exception ratio; 2: exception count)
      - grade: 0
        # Threshold corresponding to each degradation strategy. Average response time(ms), exception ratio(0-1), exception count(1-N)
        count: 1000
        # Circuit breaker duration, in seconds
        time-window: 5
        # Minimum number of requests that can trigger circuit breaking (within the valid statistical time range)
        min-request-amount: 5
        # Slow request ratio threshold in RT mode
        slow-ratio-threshold: 1.0
        # Statistical interval duration, in milliseconds
        stat-interval-ms: 1000
```

## Resilience4j

### Add Dependency

Manually add the Resilience4j dependency:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### Register Circuit Breaker Configuration

Implement the `CircuitBreakerConfigRegistrar` interface to register `CircuitBreakerConfig`:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Replace the default CircuitBreakerConfig
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Register other CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### Declarative Circuit Breaking

Configure `degrade-type=resilience4j` to enable, then declare `@Resilience4jDegrade` on the relevant interface or method:

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

### Global Resilience4j Circuit Breaker Degradation

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Use this name to retrieve CircuitBreakerConfig from CircuitBreakerConfigRegistry as the global circuit breaker configuration
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

Specify `CircuitBreakerConfig` via `circuitBreakerConfigName`, including `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` or `@Resilience4jDegrade.circuitBreakerConfigName`.

## Extending Circuit Breaker Degradation

If you need to use other circuit breaker degradation implementations, extend `BaseRetrofitDegrade` and configure it as a Spring Bean.

## Fallback and FallbackFactory

If `@RetrofitClient` does not set `fallback` or `fallbackFactory`, a `RetrofitBlockException` will be thrown directly when circuit breaking is triggered. Users can customize the method return value during circuit breaking by setting `fallback` or `fallbackFactory`.

> Note: The `fallback` class must be an implementation class of the current interface, and `fallbackFactory` must be an implementation class of `FallbackFactory<T>` with the generic parameter type being the current interface type. Additionally, `fallback` and `fallbackFactory` instances must be configured as Spring Beans.

The main difference between `fallbackFactory` and `fallback` is that `fallbackFactory` can perceive the exception cause (cause) of each circuit breaking event.

### Fallback Example

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

### FallbackFactory Example

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("Circuit breaker triggered! ", cause.getMessage(), cause);
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

[Previous: Interceptors](interceptor.md) | [Next: Error Decoder](error-decoder.md)