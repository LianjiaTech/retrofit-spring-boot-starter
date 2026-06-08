# 错误解码器
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | **繁體中文** | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

當 HTTP 發生請求錯誤（包括發生例外或者响应資料不符合預期）时，错误解码器可将 HTTP 相關信息解码到自定义例外中。

## 使用方式

在 `@RetrofitClient` 注解的 `errorDecoder()` 屬性指定當前介面的错误解码器，自定义错误解码器需要實作 `ErrorDecoder` 介面。

## 关闭 ErrorDecoder

可透過配置 `retrofit.enable-error-decoder=false` 关闭 ErrorDecoder 功能。

---

[上一節：熔断降级](degrade.md) | [下一節：指标监控（Micrometer）](metrics.md)