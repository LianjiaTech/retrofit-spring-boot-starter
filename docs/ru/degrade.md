# Circuit Breaker (предохранитель)
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | **Русский**

Circuit breaker по умолчанию отключен, в настоящее время поддерживаются две реализации: **Sentinel** и **Resilience4j**.

```yaml
retrofit:
  degrade:
    # Тип circuit breaker, по умолчанию none означает не включен
    degrade-type: sentinel
```

## Sentinel

### Добавление зависимости

Добавьте зависимость Sentinel вручную:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### Декларативный circuit breaker

Установите `degrade-type=sentinel` для включения, затем объявите аннотацию `@SentinelDegrade` на соответствующем интерфейсе или методе:

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

### Глобальный Sentinel circuit breaker

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # Стратегия circuit breaker (0: среднее время ответа; 1: дололяция исключений; 2: количество исключений)
      - grade: 0
        # Порог для каждой стратегии. Среднее время ответа(мс), дололяция исключений(0-1), количество исключений(1-N)
        count: 1000
        # Длительность circuit breaker в секундах
        time-window: 5
        # Минимальное количество запросов для активации circuit breaker (в пределах статистического интервала)
        min-request-amount: 5
        # Порог дололяции медленных запросов в режиме RT
        slow-ratio-threshold: 1.0
        # Длительность статистического интервала в миллисекундах
        stat-interval-ms: 1000
```

## Resilience4j

### Добавление зависимости

Добавьте зависимость Resilience4j вручную:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### Регистрация конфигурации circuit breaker

Реализуйте интерфейс `CircuitBreakerConfigRegistrar` для регистрации `CircuitBreakerConfig`:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Заменить стандартный CircuitBreakerConfig
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Зарегистрировать другой CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### Декларативный circuit breaker

Установите `degrade-type=resilience4j` для включения, затем объявите `@Resilience4jDegrade` на соответствующем интерфейсе или методе:

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

### Глобальный Resilience4j circuit breaker

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Имя, по которому CircuitBreakerConfig извлекается из CircuitBreakerConfigRegistry как глобальная конфигурация circuit breaker
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

Укажите `CircuitBreakerConfig` через `circuitBreakerConfigName`, включая `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` или `@Resilience4jDegrade.circuitBreakerConfigName`.

## Расширение circuit breaker

Если нужно использовать другую реализацию circuit breaker, наследуйте `BaseRetrofitDegrade` и зарегистрируйте его как Spring Bean.

## Fallback и FallbackFactory

Если в `@RetrofitClient` не указан `fallback` или `fallbackFactory`, при активации circuit breaker будет выброшено исключение `RetrofitBlockException`. Пользователь может настроить возвращаемое значение метода при circuit breaker через `fallback` или `fallbackFactory`.

> Примечание: класс `fallback` должен быть классом реализации текущего интерфейса, `fallbackFactory` должен быть классом реализации `FallbackFactory<T>`, параметр типа -- тип текущего интерфейса. Кроме того, экземпляры `fallback` и `fallbackFactory` должны быть зарегистрированы как Spring Bean.

`fallbackFactory` отличается от `fallback` главным образом тем, что может感知вать причину исключения (cause) при каждом circuit breaker.

### Пример Fallback

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

### Пример FallbackFactory

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("Circuit breaker активирован! ", cause.getMessage(), cause);
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

[Предыдущая: Интерцепторы](interceptor.md) | [Следующая: Декодер ошибок](error-decoder.md)