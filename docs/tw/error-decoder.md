# 錯誤解碼器
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | **繁體中文** | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

當 HTTP 發生請求錯誤（包括發生異常或者回應資料不符合預期）時，錯誤解碼器可將 HTTP 相關資訊解碼到自訂異常中。

## 使用方式

在 `@RetrofitClient` 註解的 `errorDecoder()` 屬性指定當前介面的錯誤解碼器，自訂錯誤解碼器需要實作 `ErrorDecoder` 介面。

## 關閉 ErrorDecoder

可透過設定 `retrofit.enable-error-decoder=false` 關閉 ErrorDecoder 功能。

---

[上一節：熔斷降級](degrade.md) | [下一節：微服務之間的 HTTP 呼叫](microservice.md)