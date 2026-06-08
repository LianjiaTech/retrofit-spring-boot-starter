# Справочник конфигурации
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | **Русский**

Компонент поддерживает多个 настраиваемых属性 для应对 различных бизнесовых сценариев. Ниже все свойства конфигурации и их значения по умолчанию:

```yaml
retrofit:
  # Глобальные фабрики конвертеров (по умолчанию JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Глобальные фабрики адаптеров (расширения CallAdapterFactory компонента встроены, не конфигурируйте повторно)
  global-call-adapter-factories:
    # ...

  # Глобальная конфигурация логирования
  global-log:
    # Включить логирование (по умолчанию false)
    enable: false
    # Глобальный уровень логирования
    log-level: info
    # Глобальная стратегия логирования (по умолчанию BASIC)
    log-strategy: basic
    # Агрегировать ли логи запросов
    aggregate: true
    # Имя лога, по умолчанию полное имя класса LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Скрытые чувствительные заголовки запроса в логах
    # По умолчанию маскируются: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Примечание: пользовательская конфигурация полностью переопределяет значения по умолчанию, необходимо самостоятельно включить элементы, которые нужно скрыть
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # Глобальная конфигурация повторных попыток
  global-retry:
    # Включить ли глобальные повторные попытки
    enable: false
    # Базовый интервал повторных попыток (мс)
    interval-ms: 100
    # Максимальное количество повторных попыток
    max-retries: 2
    # Стратегия экспоненциального роста: FIXED (фиксированный интервал, по умолчанию) / EXPONENTIAL (экспоненциальный рост)
    backoff-strategy: fixed
    # Верхний предел интервала экспоненциального роста (мс), действует только при EXPONENTIAL
    max-interval-ms: 30000
    # Коэффициент jitter [0.0, 1.0], 0.0 означает отсутствие jitter
    jitter: 0.0
    # Глобальные правила повторных попыток
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # Глобальная конфигурация таймаутов
  global-timeout:
    # Глобальный таймаут чтения (мс)
    read-timeout-ms: 10000
    # Глобальный таймаут записи (мс)
    write-timeout-ms: 10000
    # Глобальный таймаут соединения (мс)
    connect-timeout-ms: 10000
    # Глобальный таймаут полного вызова (мс), 0 означает нет таймаута
    call-timeout-ms: 0

  # Глобальная конфигурация пула соединений
  global-connection-pool:
    # Максимальное количество idle-соединений
    max-idle-connections: 5
    # Длительность keep-alive (мс)
    keep-alive-duration-ms: 300000

  # Конфигурация метрик (по умолчанию отключена; нужно явно enable=true для сборки, и в контейнере должен быть MeterRegistry)
  metrics:
    # Включить ли, по умолчанию false
    enable: false
    # Percentile для Timer
    percentiles: [0.5, 0.95, 0.99]
    # SLO histogram bucket
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # Добавить ли тег host
      host: false
      # Добавить ли тег uri
      uri: true
    # Глобальные дополнительные теги
    extra-tags:
      app: my-service
    # Префикс имени метрики
    metric-name-prefix: retrofit.client

  # Конфигурация circuit breaker
  degrade:
    # Тип circuit breaker. По умолчанию none, означает не включен
    degrade-type: none
    # Глобальная конфигурация Sentinel circuit breaker
    global-sentinel-degrade:
      # Включить ли
      enable: false
      rules:
        # Стратегия circuit breaker (0: среднее время ответа; 1: дололяция исключений; 2: количество исключений)
        - grade: 0
          # Порог для каждой стратегии. Среднее время ответа(мс), дололяция исключений(0-1), количество исключений(1-N)
          count: 1000
          # Длительность circuit breaker в секундах
          time-window: 5
          # Минимальное количество запросов для активации circuit breaker
          min-request-amount: 5
          # Порог дололяции медленных запросов в режиме RT
          slow-ratio-threshold: 1.0
          # Длительность статистического интервала в миллисекундах
          stat-interval-ms: 1000
    # Глобальная конфигурация Resilience4j circuit breaker
    global-resilience4j-degrade:
      # Включить ли
      enable: false
      # Имя, по которому CircuitBreakerConfig извлекается из CircuitBreakerConfigRegistry как глобальная конфигурация circuit breaker
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # Автоматически установить scope PathMatchInterceptor как prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # Включить ли функцию ErrorDecoder
  enable-error-decoder: true
```

В большинстве сценариев добавление上述 конфигурации в файл конфигурации Spring Boot (application.yml или application.properties) позволяет кастомизировать функциональность компонента. Если конфигурация не вступает в силу, см. [FAQ](faq.md).

**Если конфигурация Spring Boot не вступает в силу, можно手动配置 RetrofitProperties Bean**, код如下:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手动修改 retrofitProperties 各项配置值
    return retrofitProperties;
}
```

---

[Предыдущая: Кастомные аннотации RetrofitClient](custom-annotation.md) | [Следующая: Примеры использования](examples.md)