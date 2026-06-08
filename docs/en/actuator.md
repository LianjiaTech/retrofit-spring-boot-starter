# Actuator Endpoint (Expose RetrofitClient Metadata)
**English** | [简体中文](../cn/actuator.md) | [繁體中文](../tw/actuator.md) | [日本語](../ja/actuator.md) | [한국어](../ko/actuator.md) | [Español](../es/actuator.md) | [Türkçe](../tr/actuator.md) | [Русский](../ru/actuator.md)

The component provides a read-only Actuator Endpoint that exposes the complete configuration metadata of all `@RetrofitClient` interfaces in the application via `/actuator/retrofit`, making it easy to investigate "what baseUrl / timeout / logging / retry / circuit breaking config is actually in effect for a given interface".

> **Optional dependency, opt-in exposure**: The endpoint is only assembled when actuator is on the classpath (`@ConditionalOnClass`). SpringBoot 3 projects without actuator are unaffected and start normally. Endpoint exposure and the on/off switch are fully delegated to Spring Boot's standard management configuration (`@ConditionalOnAvailableEndpoint`), no custom switch is created.

## How to Enable

1. Add actuator dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. Expose the retrofit endpoint (by default actuator only exposes `health`, you must explicitly add it):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## Access Methods

| Request | Description |
|---|---|
| `GET /actuator/retrofit` | List all clients + `global` global configuration section + `count` |
| `GET /actuator/retrofit/{fully-qualified-interface-name}` | Query a single client by fully-qualified interface name; returns 404 if not matched |

## Response Structure Example

```json
{
  "count": 2,
  "global": {
    "enableErrorDecoder": true,
    "globalConverterFactories": ["retrofit2.converter.jackson.JacksonConverterFactory"],
    "timeout": { "connectMs": 10000, "readMs": 10000, "writeMs": 10000, "callMs": 0 },
    "connectionPool": { "maxIdleConnections": 5, "keepAliveDurationMs": 300000 },
    "log":     { "enable": false, "logLevel": "INFO", "logStrategy": "BASIC", "aggregate": true },
    "retry":   { "enable": false, "maxRetries": 2, "intervalMs": 100,
                 "backoffStrategy": "FIXED", "maxIntervalMs": 30000, "jitter": 0.0,
                 "retryStatusCodes": [], "retryExceptionClasses": [],
                 "retryRules": ["RESPONSE_STATUS_NOT_2XX", "OCCUR_IO_EXCEPTION"] },
    "degrade": { "degradeType": "none",
                 "sentinel":     { "enable": false, "ruleCount": 0 },
                 "resilience4j": { "enable": false, "circuitBreakerConfigName": "defaultCircuitBreakerConfig" } },
    "metrics": { "enable": false, "metricNamePrefix": "retrofit.client", "tagHost": false, "tagUri": true }
  },
  "clients": [{
    "beanName": "userService",
    "interfaceName": "com.example.UserService",
    "baseUrl": "${test.baseUrl}",
    "resolvedBaseUrl": "http://localhost:8080/api/user/",
    "serviceId": null,
    "path": null,
    "converterFactories": [],
    "callAdapterFactories": [],
    "fallback": null,
    "fallbackFactory": null,
    "errorDecoder": "com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder$DefaultErrorDecoder",
    "validateEagerly": false,
    "sourceOkHttpClient": null,
    "timeoutEffective": true,
    "timeout": { "connectMs": 3000, "readMs": 3000, "writeMs": 10000, "callMs": 0,
                 "inheritedFields": ["writeMs", "callMs"] },
    "pool":    { "maxIdleConnections": 5, "keepAliveDurationMs": 300000,
                 "inheritedFields": ["maxIdleConnections", "keepAliveDurationMs"] },
    "logging": { "source": "interface", "enable": true, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## Field Semantics

- **`resolvedBaseUrl`**: The resolved final baseUrl. Only populated when the interface has been injected and used (triggering instantiation); otherwise `null` (baseUrl is lazily resolved and not pre-resolved when not triggered).
- **`inheritedFields` in `timeout` / `pool`**: When the corresponding field on `@RetrofitClient` is configured as `-1` (default value), it means "reuse global configuration". The endpoint resolves `-1` to global fallback values using the same rules as the real build process, and records those field names in `inheritedFields` to distinguish "explicitly configured on the interface" from "inherited from global".
- **`timeoutEffective`**: `false` when the interface specifies a custom OkHttpClient via `sourceOkHttpClient` (timeouts/connection pool are then governed by the source client, and `timeout`/`pool` are not displayed).
- **`source` in `logging` / `retry`**:
  - `"interface"`: The interface has a `@Logging` / `@Retry` annotation, and the remaining fields are the expanded annotation values;
  - `"global"`: The interface has no such annotation and falls back to global configuration at runtime; values are not repeated here -- consult the top-level `global` section.
  - Note: Method-level `@Logging` / `@Retry` are not drilled down here (at runtime, method annotation takes precedence over interface, and interface over global).
- **`degrade.enabled`**: Taken from `RetrofitDegrade.isEnableDegrade(interface)`; `type` is the global `degrade.degrade-type` (`none` / `sentinel` / `resilience4j`).
- **`fallback` / `fallbackFactory`**: `null` when not configured (default `void.class`).

---

[Previous: Metrics Monitoring (Micrometer)](metrics.md) | [Next: GraalVM Native Image / AOT Support](aot.md)