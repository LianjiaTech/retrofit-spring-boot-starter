# Full Configuration Reference
**English** | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

The component supports multiple configurable properties to accommodate different business scenarios. Below are all configuration properties and their default values:

```yaml
retrofit:
  # Global converter factories (default JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Global adapter factories (the extended CallAdapterFactory is already built-in, do not configure duplicates)
  global-call-adapter-factories:
    # ...

  # Global logging configuration
  global-log:
    # Enable logging (default false)
    enable: false
    # Global logging level
    log-level: info
    # Global logging strategy (default BASIC)
    log-strategy: basic
    # Whether to aggregate request logs
    aggregate: true
    # Logger name, defaults to the fully qualified class name of LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Sensitive request headers to redact in logs
    # Default redaction: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Note: user configuration of this item entirely overrides the default values;
    # you must include any headers you still want redacted
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # Global retry configuration
  global-retry:
    # Whether to enable global retry
    enable: false
    # Global retry base interval (milliseconds)
    interval-ms: 100
    # Global maximum retry count
    max-retries: 2
    # Backoff strategy: FIXED (fixed interval, default) / EXPONENTIAL (exponential backoff)
    backoff-strategy: fixed
    # Exponential backoff interval cap (milliseconds), only effective for EXPONENTIAL
    max-interval-ms: 30000
    # Jitter coefficient [0.0, 1.0], 0.0 means no jitter
    jitter: 0.0
    # Global retry rules
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # Global timeout configuration
  global-timeout:
    # Global read timeout (milliseconds)
    read-timeout-ms: 10000
    # Global write timeout (milliseconds)
    write-timeout-ms: 10000
    # Global connect timeout (milliseconds)
    connect-timeout-ms: 10000
    # Global complete call timeout (milliseconds), 0 means no timeout
    call-timeout-ms: 0

  # Global connection pool configuration
  global-connection-pool:
    # Maximum idle connections
    max-idle-connections: 5
    # Keep-alive duration (milliseconds)
    keep-alive-duration-ms: 300000

  # Circuit breaker degradation configuration
  degrade:
    # Circuit breaker degradation type. Default none, means circuit breaker degradation is not enabled
    degrade-type: none
    # Global Sentinel degradation configuration
    global-sentinel-degrade:
      # Whether to enable
      enable: false
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
    # Global Resilience4j degradation configuration
    global-resilience4j-degrade:
      # Whether to enable
      enable: false
      # Use this name to retrieve CircuitBreakerConfig from CircuitBreakerConfigRegistry as the global circuit breaker configuration
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # Automatically set PathMatchInterceptor scope to prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # Whether to enable ErrorDecoder feature
  enable-error-decoder: true
```

In most scenarios, adding the above configuration to the Spring Boot configuration file (application.yml or application.properties) will allow you to customize the component's functionality. If you encounter issues where configuration does not take effect, see [FAQ](faq.md).

---

[Previous: Custom RetrofitClient Annotation](custom-annotation.md) | [Next: Other Feature Examples](examples.md)