# Request Retries
**English** | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

The component supports global retries and declarative retries.

## Global Retries

Global retries are disabled by default. Default configuration:

```yaml
retrofit:
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
```

### Retry Rules

Retry rules support three configurations:

1. **RESPONSE_STATUS_NOT_2XX**: Retry when the response status code is not 2xx
2. **OCCUR_IO_EXCEPTION**: Retry when an IO exception occurs
3. **OCCUR_EXCEPTION**: Retry when any exception occurs

### Backoff Strategy and Jitter

`backoffStrategy` controls how the retry interval grows. Default `FIXED` matches historical behavior:

- **FIXED**: Each retry interval is fixed at `intervalMs`
- **EXPONENTIAL**: Exponential backoff, the N-th retry interval = `intervalMs * 2^N` (N starts from 0), capped at `maxIntervalMs` to prevent unbounded interval growth

`jitter` (range `[0.0, 1.0]`, default `0.0` no jitter) adds random jitter on top of the computed delay to avoid the thundering herd problem caused by synchronized retries from multiple clients:

> Actual delay = computed delay x (1 + jitter x random), where random is a random number in `[0, 1)` range

### Conditional Triggering: by Status Code / Exception Type

On top of the coarse-grained `RetryRule`, you can further narrow the trigger conditions (default empty, matching historical behavior):

- `retryStatusCodes`: Only retry when the response status code matches the list (requires the `RESPONSE_STATUS_NOT_2XX` rule). For example `{502, 503, 504}`
- `retryExceptionClasses`: Only retry when the exception type matches the list (further narrows exceptions that already match the `RetryRule`). For example `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Declarative Retries

If only some requests need retries, you can use the `@Retry` annotation on the relevant interface or method.

## Custom Extension

If you need to modify retry behavior, you can extend `RetryInterceptor` and register it as a Spring Bean.

---

[Previous: Logging](logging.md) | [Next: Interceptors](interceptor.md)