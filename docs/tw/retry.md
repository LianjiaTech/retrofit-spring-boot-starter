# 请求重试
[English](../en/retry.md) | [简体中文](../cn/retry.md) | **繁體中文** | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

元件支援全域重试和宣告式重试。

## 全域重试

全域重试預設關閉，預設配置項如下：

```yaml
retrofit:
  global-retry:
    # 是否啟用全域重试
    enable: false
    # 全域重试基礎間隔時間（毫秒）
    interval-ms: 100
    # 全域最大重试次數
    max-retries: 2
    # 退避策略：FIXED（固定間隔，預設）/ EXPONENTIAL（指数退避）
    backoff-strategy: fixed
    # 指数退避間隔上限（毫秒），僅 EXPONENTIAL 生效
    max-interval-ms: 30000
    # 抖動系数 [0.0, 1.0]，0.0 表示無抖動
    jitter: 0.0
    # 全域重试規則
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### 重试規則

重试規則支援三種配置：

1. **RESPONSE_STATUS_NOT_2XX**：响应狀態碼不是 2xx 时執行重试
2. **OCCUR_IO_EXCEPTION**：發生 IO 例外时執行重试
3. **OCCUR_EXCEPTION**：發生任意例外时執行重试

### 退避策略与抖動

`backoffStrategy` 控制重试間隔的增長方式，預設 `FIXED` 与歷史行為一致：

- **FIXED**：每次重试間隔固定為 `intervalMs`
- **EXPONENTIAL**：指数退避，第 N 次重试間隔 = `intervalMs × 2^N`（N 從 0 起），並以 `maxIntervalMs` 封頂，避免間隔無限增長

`jitter`（取值 `[0.0, 1.0]`，預設 `0.0` 無抖動）用於在計算延遲上叠加隨機抖動，避免多客户端同步重试導致的驚群效應：

> 實際延遲 = 計算延遲 × (1 + jitter × random)，其中 random 為 `[0, 1)` 的隨機數

### 條件觸發：按狀態碼 / 例外型別

在 `RetryRule` 粗粒度規則的基礎上，可進一步收窄觸發條件（預設空，与歷史行為一致）：

- `retryStatusCodes`：僅在响应狀態碼命中列表时才重试（需配合 `RESPONSE_STATUS_NOT_2XX` 規則）。例如 `{502, 503, 504}`
- `retryExceptionClasses`：僅在例外型別命中列表时才重试（在匹配 `RetryRule` 的例外基礎上進一步收窄）。例如 `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 宣告式重试

如果只有一部分請求需要重试，可以在相應的介面或者方法上使用 `@Retry` 注解。

## 自定义擴展

如果需要修改請求重试行為，可以繼承 `RetryInterceptor`，並將其配置成 Spring Bean。

---

[上一節：日志打印](logging.md) | [下一節：拦截器](interceptor.md)