# 请求重试
[English](../en/retry.md) | **简体中文** | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

组件支持全局重试和声明式重试。

## 全局重试

全局重试默认关闭，默认配置项如下：

```yaml
retrofit:
  global-retry:
    # 是否启用全局重试
    enable: false
    # 全局重试基础间隔时间（毫秒）
    interval-ms: 100
    # 全局最大重试次数
    max-retries: 2
    # 退避策略：FIXED（固定间隔，默认）/ EXPONENTIAL（指数退避）
    backoff-strategy: fixed
    # 指数退避间隔上限（毫秒），仅 EXPONENTIAL 生效
    max-interval-ms: 30000
    # 抖动系数 [0.0, 1.0]，0.0 表示无抖动
    jitter: 0.0
    # 全局重试规则
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### 重试规则

重试规则支持三种配置：

1. **RESPONSE_STATUS_NOT_2XX**：响应状态码不是 2xx 时执行重试
2. **OCCUR_IO_EXCEPTION**：发生 IO 异常时执行重试
3. **OCCUR_EXCEPTION**：发生任意异常时执行重试

### 退避策略与抖动

`backoffStrategy` 控制重试间隔的增长方式，默认 `FIXED` 与历史行为一致：

- **FIXED**：每次重试间隔固定为 `intervalMs`
- **EXPONENTIAL**：指数退避，第 N 次重试间隔 = `intervalMs × 2^N`（N 从 0 起），并以 `maxIntervalMs` 封顶，避免间隔无限增长

`jitter`（取值 `[0.0, 1.0]`，默认 `0.0` 无抖动）用于在计算延迟上叠加随机抖动，避免多客户端同步重试导致的惊群效应：

> 实际延迟 = 计算延迟 × (1 + jitter × random)，其中 random 为 `[0, 1)` 的随机数

### 条件触发：按状态码 / 异常类型

在 `RetryRule` 粗粒度规则的基础上，可进一步收窄触发条件（默认空，与历史行为一致）：

- `retryStatusCodes`：仅在响应状态码命中列表时才重试（需配合 `RESPONSE_STATUS_NOT_2XX` 规则）。例如 `{502, 503, 504}`
- `retryExceptionClasses`：仅在异常类型命中列表时才重试（在匹配 `RetryRule` 的异常基础上进一步收窄）。例如 `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 声明式重试

如果只有一部分请求需要重试，可以在相应的接口或者方法上使用 `@Retry` 注解。

## 自定义扩展

如果需要修改请求重试行为，可以继承 `RetryInterceptor`，并将其配置成 Spring Bean。

---

[上一节：日志打印](logging.md) | [下一节：拦截器](interceptor.md)