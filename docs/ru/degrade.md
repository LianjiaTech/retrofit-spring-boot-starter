# Обрыв цепи/деградация
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | **Русский**

Обрыв цепи/деградация по умолчанию отключен. В настоящее время поддерживаются две реализации: **Sentinel** и **Resilience4j**.

```yaml
retrofit:
  degrade:
    # Тип обрыва цепи/деградации, по умолчанию none — не включен
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

### Декларативный обрыв цепи

Включите, настроив `degrade-type=sentinel`, затем объявите аннотацию `@SentinelDegrade` на соответствующих интерфейсах или методах:

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

### Глобальный обрыв цепи/деградация Sentinel

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # Стратегия деградации (0: среднее время ответа; 1: пропорция исключений; 2: количество исключений)
      - grade: 0
        # Пороговое значение для каждой стратегии деградации. Среднее время ответа (мс), пропорция исключений (0-1), количество исключений (1-N)
        count: 1000
        # Длительность обрыва цепи, в секундах
        time-window: 5
        # Минимальное количество запросов (в пределах действительного статистического временного диапазона), способных триггерить обрыв цепи
        min-request-amount: 5
        # Пороговое значение пропорции медленных запросов в режиме RT
        slow-ratio-threshold: 1.0
        # Длительность статистического интервала, в миллисекундах
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

### Регистрация конфигурации обрыва цепи

Реализуйте интерфейс `CircuitBreakerConfigRegistrar` для регистрации `CircuitBreakerConfig`:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Заменить стандартный CircuitBreakerConfig
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Зарегистрировать другие CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### Декларативный обрыв цепи

Включите, настроив `degrade-type=resilience4j`, затем объявите `@Resilience4jDegrade` на соответствующих интерфейсах или методах:

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

### Глобальный обрыв цепи/деградация Resilience4j

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Получить CircuitBreakerConfig из CircuitBreakerConfigRegistry по этому имени как глобальную конфигурацию обрыва цепи
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

Укажите `CircuitBreakerConfig` через `circuitBreakerConfigName`, включая `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` или `@Resilience4jDegrade.circuitBreakerConfigName`.

## Расширение обрыва цепи/деградации

Если нужно использовать другую реализацию обрыва цепи/деградации, наследуйте `BaseRetrofitDegrade` и зарегистрируйте его как Spring Bean.

## Fallback и FallbackFactory

Если `@RetrofitClient` не устанавливает `fallback` или `fallbackFactory`, при триггере обрыва цепи напрямую выбрасывается исключение `RetrofitBlockException`. Пользователь может настроить возвращаемое значение метода при обрыве цепи, установив `fallback` или `fallbackFactory`.

> Примечание: класс `fallback` должен быть классом реализации текущего интерфейса, `fallbackFactory` должен быть классом реализации `FallbackFactory<T>` с параметром типа текущего интерфейса. Кроме того, экземпляры `fallback` и `fallbackFactory` должны быть зарегистрированы как Spring Bean.

Основное отличие `fallbackFactory` от `fallback` заключается в том, что `fallbackFactory` может感知 причину исключения (cause) каждого обрыва цепи.

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
        log.error("Обрыв цепи триггерирован! ", cause.getMessage(), cause);
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

Предыдущий: [Интерцепторы](interceptor.md) | Следующий: [Декодер ошибок](error-decoder.md)