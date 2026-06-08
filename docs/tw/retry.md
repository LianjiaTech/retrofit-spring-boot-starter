# 請求重試
[English](../en/retry.md) | [简体中文](../cn/retry.md) | **繁體中文** | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

元件支援全域重試和宣告式重試。

## 全域重試

全域重試預設關閉，預設設定項如下：

```yaml
retrofit:
  global-retry:
    # 是否啟用全域重試
    enable: false
    # 全域重試基礎間隔時間（毫秒）
    interval-ms: 100
    # 全域最大重試次數
    max-retries: 2
    # 退避策略：FIXED（固定間隔，預設）/ EXPONENTIAL（指數退避）
    backoff-strategy: fixed
    # 指數退避間隔上限（毫秒），僅 EXPONENTIAL 生效
    max-interval-ms: 30000
    # 抖動係數 [0.0, 1.0]，0.0 表示無抖動
    jitter: 0.0
    # 全域重試規則
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### 重試規則

重試規則支援三種設定：

1. **RESPONSE_STATUS_NOT_2XX**：回應狀態碼不是 2xx 時執行重試
2. **OCCUR_IO_EXCEPTION**：發生 IO 異常時執行重試
3. **OCCUR_EXCEPTION**：發生任意異常時執行重試

### 退避策略與抖動

`backoffStrategy` 控制重試間隔的增長方式，預設 `FIXED` 與歷史行為一致：

- **FIXED**：每次重試間隔固定為 `intervalMs`
- **EXPONENTIAL**：指數退避，第 N 次重試間隔 = `intervalMs × 2^N`（N 從 0 起），並以 `maxIntervalMs` 封頂，避免間隔無限增長

`jitter`（取值 `[0.0, 1.0]`，預設 `0.0` 無抖動）用於在計算延遲上疊加隨機抖動，避免多客戶端同步重試導致的驚群效應：

> 實際延遲 = 計算延遲 × (1 + jitter × random)，其中 random 為 `[0, 1)` 的隨機數

### 條件觸發：按狀態碼 / 異常類型

在 `RetryRule` 粗粒度規則的基礎上，可進一步收窄觸發條件（預設空，與歷史行為一致）：

- `retryStatusCodes`：僅在回應狀態碼命中列表時才重試（需配合 `RESPONSE_STATUS_NOT_2XX` 規則）。例如 `{502, 503, 504}`
- `retryExceptionClasses`：僅在異常類型命中列表時才重試（在匹配 `RetryRule` 的異常基礎上進一步收窄）。例如 `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 宣告式重試

如果只有一部分請求需要重試，可以在相應的介面或者方法上使用 `@Retry` 註解。

## 自訂擴充

如果需要修改請求重試行為，可以繼承 `RetryInterceptor`，並將其設定成 Spring Bean。

---

[上一節：日誌打印](logging.md) | [下一節：攔截器](interceptor.md)