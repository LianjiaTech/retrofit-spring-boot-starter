# 日誌打印
[English](../en/logging.md) | [简体中文](../cn/logging.md) | **繁體中文** | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

元件支援全域日誌打印和宣告式日誌打印。

## 全域日誌打印

全域日誌打印預設關閉（`enable=false`），需要主動開啟。開啟後預設按 `BASIC` 策略僅打印請求/回應行（含狀態碼與耗時），開銷可忽略。預設設定如下：

```yaml
retrofit:
  global-log:
    # 啟用日誌打印（預設 false）
    enable: false
    # 全域日誌打印級別
    log-level: info
    # 全域日誌打印策略（預設 BASIC，僅打印請求/回應行）
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
```

四種日誌打印策略含義如下：

1. **NONE**：不打印日誌
2. **BASIC**：僅打印請求和回應行
3. **HEADERS**：打印請求和回應行及其請求頭/回應頭
4. **BODY**：打印請求和回應行、請求頭/回應頭以及請求體/回應體（如存在）

## 宣告式日誌打印

如果只需要部分請求才打印日誌，可以在相關介面或者方法上使用 `@Logging` 註解。

## 自訂擴充

如果需要修改日誌打印行為，可以繼承 `LoggingInterceptor`，並將其設定成 Spring Bean。

---

[上一節：方法級超時設定](timeout.md) | [下一節：請求重試](retry.md)