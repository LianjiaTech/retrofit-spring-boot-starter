# Actuator Endpoint（暴露 RetrofitClient 元信息）
[English](../en/actuator.md) | [简体中文](../cn/actuator.md) | **繁體中文** | [日本語](../ja/actuator.md) | [한국어](../ko/actuator.md) | [Español](../es/actuator.md) | [Türkçe](../tr/actuator.md) | [Русский](../ru/actuator.md)

元件提供了一個基於 Spring Boot Actuator 的唯讀 Endpoint，透過 `/actuator/retrofit` 暴露應用中所有 `@RetrofitClient` 介面的完整配置元信息，便於排查"某個介面實際生效的 baseUrl / 超时 / 日志 / 重试 / 熔断配置到底是什麼"。

> **可選依賴、按需啟用**：僅當使用者引入 actuator 时該 Endpoint 才會裝配（`@ConditionalOnClass`），未引入 actuator 的 SpringBoot 3 專案不受任何影響、正常啟動。Endpoint 的暴露与開關完全交给 Spring Boot 標準的 management 配置（`@ConditionalOnAvailableEndpoint`），不自造開關。

## 啟用方式

1. 引入 actuator：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. 暴露 retrofit endpoint（預設 actuator 僅暴露 `health`，需明確加入）：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## 存取方式

| 請求 | 说明 |
|---|---|
| `GET /actuator/retrofit` | 列出所有 client + `global` 全域配置段 + `count` |
| `GET /actuator/retrofit/{介面全限定名}` | 按介面全限定名查詢單個 client，未匹配回傳 404 |

## 响应结构示例

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
    "logging": { "source": "interface", "enable": 1, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## 字段语义说明

- **`resolvedBaseUrl`**：已解析的最終 baseUrl。僅當該介面已被注入使用（觸發过實例化）时才有值，否則為 `null`（baseUrl 為懶解析，未觸發时不預先解析）。
- **`timeout` / `pool` 的 `inheritedFields`**：`@RetrofitClient` 上對應字段配置為 `-1`（預設值）时表示"復用全域配置"。Endpoint 会按与真實構建一致的規則把 `-1` 解析為全域兜底值，並把這些字段名記入 `inheritedFields`，便於區分"介面明確配置"還是"繼承全域"。
- **`timeoutEffective`**：當介面透過 `sourceOkHttpClient` 指定了自定义 OkHttpClient 时為 `false`（此时超时/连接池由源客户端決定，`timeout`/`pool` 不展示）。
- **`logging` / `retry` 的 `source`**：
  - `"interface"`：介面上存在 `@Logging` / `@Retry` 注解，其餘字段為注解展開值；
  - `"global"`：介面無對應注解、執行時回落到全域配置，此时不重複展開值，請查閱頂層 `global` 段。
  - 注意：方法級 `@Logging` / `@Retry` 不在此處下鑽展示（執行時方法注解優先於介面、介面優先於全域）。
- **`degrade.enabled`**：取自 `RetrofitDegrade.isEnableDegrade(介面)`；`type` 為全域 `degrade.degrade-type`（`none` / `sentinel` / `resilience4j`）。
- **`fallback` / `fallbackFactory`**：未配置（預設 `void.class`）时為 `null`。

---

[上一節：指标监控（Micrometer）](metrics.md) | [下一節：GraalVM Native Image / AOT 支持](aot.md)