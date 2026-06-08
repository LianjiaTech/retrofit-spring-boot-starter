# Logging
**English** | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

The component supports global logging and declarative logging.

## Global Logging

Global logging is disabled by default (`enable=false`) and must be explicitly enabled. Once enabled, it defaults to the `BASIC` strategy, which only prints request/response lines (including status code and duration), with negligible overhead. The default configuration is as follows:

```yaml
retrofit:
  global-log:
    # Enable logging (default false)
    enable: false
    # Global logging level
    log-level: info
    # Global logging strategy (default BASIC, only prints request/response lines)
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
```

The four logging strategies are defined as follows:

1. **NONE**: No logging
2. **BASIC**: Only prints request and response lines
3. **HEADERS**: Prints request and response lines along with their headers
4. **BODY**: Prints request and response lines, headers, and request/response bodies (if present)

## Declarative Logging

If you only need to log certain requests, you can use the `@Logging` annotation on the relevant interface or method.

## Custom Extension

If you need to modify the logging behavior, you can extend `LoggingInterceptor` and configure it as a Spring Bean.

---

[Previous: Method-Level Timeout Configuration](timeout.md) | [Next: Request Retry](retry.md)