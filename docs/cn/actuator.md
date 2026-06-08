# Actuator Endpoint（暴露 RetrofitClient 元信息）
[English](../en/actuator.md) | **简体中文** | [繁體中文](../tw/actuator.md) | [日本語](../ja/actuator.md) | [한국어](../ko/actuator.md) | [Español](../es/actuator.md) | [Türkçe](../tr/actuator.md) | [Русский](../ru/actuator.md)

组件提供了一个基于 Spring Boot Actuator 的只读 Endpoint，通过 `/actuator/retrofit` 暴露应用中所有 `@RetrofitClient` 接口的完整配置元信息，便于排查"某个接口实际生效的 baseUrl / 超时 / 日志 / 重试 / 熔断配置到底是什么"。

> **可选依赖、按需启用**：仅当用户引入 actuator 时该 Endpoint 才会装配（`@ConditionalOnClass`），未引入 actuator 的 SpringBoot 3 项目不受任何影响、正常启动。Endpoint 的暴露与开关完全交给 Spring Boot 标准的 management 配置（`@ConditionalOnAvailableEndpoint`），不自造开关。

## 启用方式

1. 引入 actuator：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. 暴露 retrofit endpoint（默认 actuator 仅暴露 `health`，需显式加入）：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## 访问方式

| 请求 | 说明 |
|---|---|
| `GET /actuator/retrofit` | 列出所有 client + `global` 全局配置段 + `count` |
| `GET /actuator/retrofit/{接口全限定名}` | 按接口全限定名查询单个 client，未匹配返回 404 |

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
    "logging": { "source": "interface", "enable": true, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## 字段语义说明

- **`resolvedBaseUrl`**：已解析的最终 baseUrl。仅当该接口已被注入使用（触发过实例化）时才有值，否则为 `null`（baseUrl 为懒解析，未触发时不预先解析）。
- **`timeout` / `pool` 的 `inheritedFields`**：`@RetrofitClient` 上对应字段配置为 `-1`（默认值）时表示"复用全局配置"。Endpoint 会按与真实构建一致的规则把 `-1` 解析为全局兜底值，并把这些字段名记入 `inheritedFields`，便于区分"接口显式配置"还是"继承全局"。
- **`timeoutEffective`**：当接口通过 `sourceOkHttpClient` 指定了自定义 OkHttpClient 时为 `false`（此时超时/连接池由源客户端决定，`timeout`/`pool` 不展示）。
- **`logging` / `retry` 的 `source`**：
  - `"interface"`：接口上存在 `@Logging` / `@Retry` 注解，其余字段为注解展开值；
  - `"global"`：接口无对应注解、运行时回落到全局配置，此时不重复展开值，请查阅顶层 `global` 段。
  - 注意：方法级 `@Logging` / `@Retry` 不在此处下钻展示（运行时方法注解优先于接口、接口优先于全局）。
- **`degrade.enabled`**：取自 `RetrofitDegrade.isEnableDegrade(接口)`；`type` 为全局 `degrade.degrade-type`（`none` / `sentinel` / `resilience4j`）。
- **`fallback` / `fallbackFactory`**：未配置（默认 `void.class`）时为 `null`。

---

[上一节：指标监控（Micrometer）](metrics.md) | [下一节：GraalVM Native Image / AOT 支持](aot.md)