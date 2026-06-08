# 全量設定項參考
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | **繁體中文** | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

元件支援多個可設定屬性，用來應對不同的業務場景。以下是所有設定屬性及預設值：

```yaml
retrofit:
  # 全域轉換器工廠（預設 JacksonConverterFactory）
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # 全域適配器工廠（元件擴充的 CallAdapterFactory 已內建，請勿重複設定）
  global-call-adapter-factories:
    # ...

  # 全域日誌打印設定
  global-log:
    # 啟用日誌打印（預設 false）
    enable: false
    # 全域日誌打印級別
    log-level: info
    # 全域日誌打印策略（預設 BASIC）
    log-strategy: basic
    # 是否聚合打印請求日誌
    aggregate: true
    # 日誌名稱，預設為 LoggingInterceptor 的全類名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 日誌中需要隱藏的敏感請求頭
    # 預設遮蔽：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：使用者設定該項會整體覆寫預設值，需自行包含仍要遮蔽的項
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # 全域重試設定
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

  # 全域超時時間設定
  global-timeout:
    # 全域讀取超時時間（毫秒）
    read-timeout-ms: 10000
    # 全域寫入超時時間（毫秒）
    write-timeout-ms: 10000
    # 全域連線超時時間（毫秒）
    connect-timeout-ms: 10000
    # 全域完整呼叫超時時間（毫秒），0 表示無超時
    call-timeout-ms: 0

  # 全域連線池設定
  global-connection-pool:
    # 最大空閒連線數
    max-idle-connections: 5
    # 保持連線時長（毫秒）
    keep-alive-duration-ms: 300000

  # 熔斷降級設定
  degrade:
    # 熔斷降級類型。預設 none，表示不啟用熔斷降級
    degrade-type: none
    # 全域 Sentinel 降級設定
    global-sentinel-degrade:
      # 是否開啟
      enable: false
      rules:
        # 降級策略（0：平均回應時間；1：異常比例；2：異常數量）
        - grade: 0
          # 各降級策略對應的阈值。平均回應時間(ms)，異常比例(0-1)，異常數量(1-N)
          count: 1000
          # 熔斷時長，單位為 s
          time-window: 5
          # （在有效統計時間範圍內）能夠觸發熔斷的最小請求數
          min-request-amount: 5
          # RT 模式下慢請求率的阈值
          slow-ratio-threshold: 1.0
          # 時間間隔統計持續時間，單位為毫秒
          stat-interval-ms: 1000
    # 全域 Resilience4j 降級設定
    global-resilience4j-degrade:
      # 是否開啟
      enable: false
      # 根據該名稱從 CircuitBreakerConfigRegistry 取得 CircuitBreakerConfig，作為全域熔斷設定
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # 自動設定 PathMatchInterceptor 的 scope 為 prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # 是否開啟 ErrorDecoder 功能
  enable-error-decoder: true
```

絕大部分場景下，在 Spring Boot 設定檔案（application.yml 或者 application.properties）中加上上述設定，即可自訂修改元件功能。如遇設定無法生效等問題，參見[常見問題](faq.md)。

---

[上一節：自訂 RetrofitClient 註解](custom-annotation.md) | [下一節：其他功能範例](examples.md)