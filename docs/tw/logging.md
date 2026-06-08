# 日志打印
[English](../en/logging.md) | [简体中文](../cn/logging.md) | **繁體中文** | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

元件支援全域日志打印和宣告式日志打印。

## 全域日志打印

全域日志打印預設關閉（`enable=false`），需要主動開啟。開啟後預設按 `BASIC` 策略僅打印請求/响应行（含狀態碼与耗時），開銷可忽略。預設配置如下：

```yaml
retrofit:
  global-log:
    # 啟用日志打印（預設 false）
    enable: false
    # 全域日志打印級別
    log-level: info
    # 全域日志打印策略（預設 BASIC，僅打印請求/响应行）
    log-strategy: basic
    # 是否聚合打印請求日志
    aggregate: true
    # 日志名稱，預設為 LoggingInterceptor 的全類名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 日志中需要隱藏的敏感請求頭
    # 預設遮蔽：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：使用者配置該項會整體覆蓋預設值，需自行包含仍要遮蔽的項
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

四種日志打印策略含義如下：

1. **NONE**：不打印日志
2. **BASIC**：僅打印請求和响应行
3. **HEADERS**：打印請求和响应行及其請求頭/响应頭
4. **BODY**：打印請求和响应行、請求頭/响应頭以及請求體/响应體（如存在）

## 宣告式日志打印

如果只需要部分請求才打印日志，可以在相關介面或者方法上使用 `@Logging` 注解。

## 自定义擴展

如果需要修改日志打印行為，可以繼承 `LoggingInterceptor`，並將其配置成 Spring Bean。

---

[上一節：方法级超时配置](timeout.md) | [下一節：请求重试](retry.md)