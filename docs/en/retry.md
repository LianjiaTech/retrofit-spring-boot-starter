# Request Retry
**English** | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

The component supports global retry and declarative retry.

## Global Retry

Global retry is disabled by default. The default configuration items are as follows:

```yaml
retrofit:
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
```

### Retry Rules

Retry rules support three configurations:

1. **RESPONSE_STATUS_NOT_2XX**: Retry when the response status code is not 2xx
2. **OCCUR_IO_EXCEPTION**: Retry when an IO exception occurs
3. **OCCUR_EXCEPTION**: Retry when any exception occurs

### Backoff Strategy and Jitter

`backoffStrategy` controls how the retry interval grows. The default `FIXED` is consistent with historical behavior:

- **FIXED**: Each retry interval is fixed at `intervalMs`
- **EXPONENTIAL**: Exponential backoff, the Nth retry interval = `intervalMs × 2^N` (N starts from 0), capped by `maxIntervalMs` to prevent unbounded interval growth

`jitter` (range `[0.0, 1.0]`, default `0.0` for no jitter) adds random jitter on top of the calculated delay to avoid the thundering herd effect caused by synchronized retries from multiple clients:

> Actual delay = calculated delay × (1 + jitter × random), where random is a random number in `[0, 1)`

### Conditional Triggering: By Status Code / Exception Type

On top of the coarse-grained `RetryRule` rules, you can further narrow the trigger conditions (default empty, consistent with historical behavior):

- `retryStatusCodes`: Only retry when the response status code matches the list (must be used with the `RESPONSE_STATUS_NOT_2XX` rule). For example `{502, 503, 504}`
- `retryExceptionClasses`: Only retry when the exception type matches the list (further narrowing on top of the `RetryRule`-matched exceptions). For example `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Declarative Retry

If only some requests need retry, you can use the `@Retry` annotation on the relevant interface or method.

## Custom Extension

If you need to modify the request retry behavior, you can extend `RetryInterceptor` and configure it as a Spring Bean.

---

[Previous: Logging](logging.md) | [Next: Interceptors](interceptor.md)