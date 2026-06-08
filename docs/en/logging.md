# Logging
**English** | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

The component supports global logging and declarative logging.

## Global Logging

Global logging is disabled by default (`enable=false`) and must be explicitly enabled. Once enabled, the default `BASIC` strategy only logs request/response lines (including status code and elapsed time), with negligible overhead. Default configuration:

```yaml
retrofit:
  global-log:
    # Enable logging (default false)
    enable: false
    # Global log level
    log-level: info
    # Global log strategy (default BASIC, only logs request/response lines)
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
```

The four logging strategies are as follows:

1. **NONE**: No logs
2. **BASIC**: Only logs request and response lines
3. **HEADERS**: Logs request and response lines along with their headers
4. **BODY**: Logs request and response lines, headers, and bodies (if present)

## Declarative Logging

If you only need logging for certain requests, you can use the `@Logging` annotation on the relevant interface or method.

## Custom Extension

If you need to modify logging behavior, you can extend `LoggingInterceptor` and register it as a Spring Bean.

---

[Previous: Method-level Timeout Configuration](timeout.md) | [Next: Request Retries](retry.md)