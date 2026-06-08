# Справочник всех конфигураций
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | **Русский**

Компонент поддерживает множество настраиваемых свойств для различных бизнес-сценариев. Ниже приведены все свойства конфигурации и их значения по умолчанию:

```yaml
retrofit:
  # Глобальные фабрики конвертеров (по умолчанию JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Глобальные фабрики адаптеров (расширённые CallAdapterFactory компонента уже встроены, не добавляйте повторно)
  global-call-adapter-factories:
    # ...

  # Конфигурация глобального логирования
  global-log:
    # Включить логирование (по умолчанию false)
    enable: false
    # Уровень глобального логирования
    log-level: info
    # Стратегия глобального логирования (по умолчанию BASIC)
    log-strategy: basic
    # Агрегировать ли логи запросов
    aggregate: true
    # Имя лога, по умолчанию полное имя класса LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Конфиденциальные заголовки запроса, которые нужно скрыть в логах
    # По умолчанию скрываются: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Примечание: пользовательская конфигурация полностью заменяет значения по умолчанию, необходимо самостоятельно включить элементы, которые нужно продолжать скрывать
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # Конфигурация глобальных повторных попыток
  global-retry:
    # Включить ли глобальные повторные попытки
    enable: false
    # Базовый интервал глобальных повторных попыток (миллисекунды)
    interval-ms: 100
    # Максимальное количество глобальных повторных попыток
    max-retries: 2
    # Стратегия退避: FIXED (фиксированный интервал, по умолчанию) / EXPONENTIAL (экспоненциальный退避)
    backoff-strategy: fixed
    # Верхний предел интервала экспоненциального退避 (миллисекунды), действует только для EXPONENTIAL
    max-interval-ms: 30000
    # Коэффициент джиттера [0.0, 1.0], 0.0 означает отсутствие джиттера
    jitter: 0.0
    # Правила глобальных повторных попыток
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # Конфигурация глобальных таймаутов
  global-timeout:
    # Глобальный таймаут чтения (миллисекунды)
    read-timeout-ms: 10000
    # Глобальный таймаут записи (миллисекунды)
    write-timeout-ms: 10000
    # Глобальный таймаут подключения (миллисекунды)
    connect-timeout-ms: 10000
    # Глобальный таймаут полного вызова (миллисекунды), 0 означает отсутствие таймаута
    call-timeout-ms: 0

  # Конфигурация глобального пула подключений
  global-connection-pool:
    # Максимальное количество idle-подключений
    max-idle-connections: 5
    # Длительность сохранения подключения (миллисекунды)
    keep-alive-duration-ms: 300000

  # Конфигурация обрыва цепи/деградации
  degrade:
    # Тип обрыва цепи/деградации. По умолчанию none — обрыв цепи/деградация не включен
    degrade-type: none
    # Глобальная конфигурация деградации Sentinel
    global-sentinel-degrade:
      # Включить или нет
      enable: false
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
    # Глобальная конфигурация деградации Resilience4j
    global-resilience4j-degrade:
      # Включить или нет
      enable: false
      # Получить CircuitBreakerConfig из CircuitBreakerConfigRegistry по этому имени как глобальную конфигурацию обрыва цепи
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # Автоматическая установка scope PathMatchInterceptor как prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # Включить ли функцию ErrorDecoder
  enable-error-decoder: true
```

В большинстве сценариев достаточно добавить приведённую выше конфигурацию в конфигурационный файл Spring Boot (application.yml или application.properties), чтобы изменить функциональность компонента. Если конфигурация не работает, см. [Часто задаваемые вопросы](faq.md).

---

Предыдущий: [Пользовательская аннотация RetrofitClient](custom-annotation.md) | Следующий: [Примеры других функций](examples.md)