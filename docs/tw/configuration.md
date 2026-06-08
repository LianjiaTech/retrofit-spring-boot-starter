# 全量配置项参考
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | **繁體中文** | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

元件支援多個可配置屬性，用來應對不同的業務場景。以下是所有配置屬性及預設值：

```yaml
retrofit:
  # 全域轉換器工廠（預設 JacksonConverterFactory）
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # 全域适配器工廠（元件擴展的 CallAdapterFactory 已內建，請勿重複配置）
  global-call-adapter-factories:
    # ...

  # 全域日志打印配置
  global-log:
    # 啟用日志打印（預設 false）
    enable: false
    # 全域日志打印級別
    log-level: info
    # 全域日志打印策略（預設 BASIC）
    log-strategy: basic
    # 是否聚合打印請求日志
    aggregate: true
    # 日志名稱，預設為 LoggingInterceptor 的全類名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 日志中需要隱藏的敏感請求頭
    # 預設遮蔽：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：使用者配置該項会整體覆蓋預設值，需自行包含仍要遮蔽的項
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # 全域重试配置
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

  # 全域超时時間配置
  global-timeout:
    # 全域讀取超时時間（毫秒）
    read-timeout-ms: 10000
    # 全域寫入超时時間（毫秒）
    write-timeout-ms: 10000
    # 全域連線超时時間（毫秒）
    connect-timeout-ms: 10000
    # 全域完整呼叫超时時間（毫秒），0 表示無超时
    call-timeout-ms: 0

  # 全域连接池配置
  global-connection-pool:
    # 最大空閒连接數
    max-idle-connections: 5
    # 保持連線时长（毫秒）
    keep-alive-duration-ms: 300000

  # 指标監控配置（預設關閉；需要明確 enable=true 才會裝配，且容器內必須有 MeterRegistry）
  metrics:
    # 是否啟用，預設 false
    enable: false
    # Timer 分位數
    percentiles: [0.5, 0.95, 0.99]
    # SLO 直方圖分桶
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # 是否带 host 标签
      host: false
      # 是否带 uri 标签
      uri: true
    # 全域附加标签
    extra-tags:
      app: my-service
    # 指标名前缀
    metric-name-prefix: retrofit.client

  # 熔断降级配置
  degrade:
    # 熔断降级型別。預設 none，表示不啟用熔断降级
    degrade-type: none
    # 全域 Sentinel 降级配置
    global-sentinel-degrade:
      # 是否開啟
      enable: false
      rules:
        # 降级策略（0：平均响应時間；1：例外比例；2：例外數量）
        - grade: 0
          # 各降级策略對應的阈值。平均响应時間(ms)，例外比例(0-1)，例外數量(1-N)
          count: 1000
          # 熔断时长，单位為 s
          time-window: 5
          # （在有效統計時間範圍内）能夠觸發熔断的最小請求數
          min-request-amount: 5
          # RT 模式下慢請求率的阈值
          slow-ratio-threshold: 1.0
          # 時間間隔統計持續時間，单位為毫秒
          stat-interval-ms: 1000
    # 全域 Resilience4j 降级配置
    global-resilience4j-degrade:
      # 是否開啟
      enable: false
      # 根據該名稱從 CircuitBreakerConfigRegistry 取得 CircuitBreakerConfig，作為全域熔断配置
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # 自動設定 PathMatchInterceptor 的 scope 為 prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # 是否開啟 ErrorDecoder 功能
  enable-error-decoder: true
```

絕大部分場景下，在 Spring Boot 配置檔案（application.yml 或者 application.properties）中加上上述配置，即可自定义修改元件功能。如遇配置無法生效等問題，參見[常見問題](faq.md)。

**如果 Spring Boot 配置檔案無法生效，可以手動配置 RetrofitProperties Bean**，程式碼如下：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手動修改 retrofitProperties 各項配置值
    return retrofitProperties;
}
```

---

[上一節：自定义 RetrofitClient 注解](custom-annotation.md) | [下一節：其他功能示例](examples.md)