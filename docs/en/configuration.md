# Configuration Properties Reference
**English** | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

The component supports multiple configurable properties to accommodate different business scenarios. Below are all configuration properties and their default values:

```yaml
retrofit:
  # Global converter factories (default JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Global call adapter factories (component-extended CallAdapterFactory is pre-included, do not reconfigure)
  global-call-adapter-factories:
    # ...

  # Global logging configuration
  global-log:
    # Enable logging (default false)
    enable: false
    # Global log level
    log-level: info
    # Global log strategy (default BASIC)
    log-strategy: basic
    # Whether to aggregate request logs
    aggregate: true
    # Logger name, default is the fully qualified class name of LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Sensitive request headers to redact in logs
    # Default redacted: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Note: user configuration of this property entirely overrides the defaults, include items you still want redacted
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # Global retry configuration
  global-retry:
    # Whether to enable global retries
    enable: false
    # Global retry base interval (ms)
    interval-ms: 100
    # Global max retry count
    max-retries: 2
    # Backoff strategy: FIXED (constant interval, default) / EXPONENTIAL (exponential backoff)
    backoff-strategy: fixed
    # Exponential backoff interval cap (ms), only effective for EXPONENTIAL
    max-interval-ms: 30000
    # Jitter factor [0.0, 1.0], 0.0 means no jitter
    jitter: 0.0
    # Global retry rules
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # Global timeout configuration
  global-timeout:
    # Global read timeout (ms)
    read-timeout-ms: 10000
    # Global write timeout (ms)
    write-timeout-ms: 10000
    # Global connect timeout (ms)
    connect-timeout-ms: 10000
    # Global full call timeout (ms), 0 means no timeout
    call-timeout-ms: 0

  # Global connection pool configuration
  global-connection-pool:
    # Maximum idle connections
    max-idle-connections: 5
    # Keep-alive duration (ms)
    keep-alive-duration-ms: 300000

  # Metrics monitoring configuration (disabled by default; must explicitly set enable=true, and MeterRegistry must exist in container)
  metrics:
    # Whether to enable, default false
    enable: false
    # Timer percentiles
    percentiles: [0.5, 0.95, 0.99]
    # SLO histogram bucket boundaries
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # Whether to include host tag
      host: false
      # Whether to include uri tag
      uri: true
    # Global extra tags
    extra-tags:
      app: my-service
    # Metric name prefix
    metric-name-prefix: retrofit.client

  # Circuit breaking configuration
  degrade:
    # Circuit breaker type. Default none, meaning circuit breaking is not enabled
    degrade-type: none
    # Global Sentinel circuit breaking configuration
    global-sentinel-degrade:
      # Whether to enable
      enable: false
      rules:
        # Degradation strategy (0: average response time; 1: exception ratio; 2: exception count)
        - grade: 0
          # Threshold for each degradation strategy. Average response time(ms), exception ratio(0-1), exception count(1-N)
          count: 1000
          # Circuit breaking duration, in seconds
          time-window: 5
          # Minimum request count that can trigger circuit breaking (within valid statistical time range)
          min-request-amount: 5
          # Slow request ratio threshold in RT mode
          slow-ratio-threshold: 1.0
          # Statistical interval duration, in milliseconds
          stat-interval-ms: 1000
    # Global Resilience4j circuit breaking configuration
    global-resilience4j-degrade:
      # Whether to enable
      enable: false
      # Fetch CircuitBreakerConfig by this name from CircuitBreakerConfigRegistry as the global circuit breaker configuration
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # Automatically set PathMatchInterceptor scope to prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # Whether to enable ErrorDecoder feature
  enable-error-decoder: true
```

In most scenarios, adding the above configuration in the Spring Boot configuration file (application.yml or application.properties) is sufficient to customize the component's functionality. If you encounter issues where configuration does not take effect, see [FAQ](faq.md).

**If Spring Boot configuration files do not take effect, you can manually configure the RetrofitProperties Bean** with the following code:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // Manually modify retrofitProperties configuration values
    return retrofitProperties;
}
```

---

[Previous: Custom RetrofitClient Annotations](custom-annotation.md) | [Next: Additional Examples](examples.md)