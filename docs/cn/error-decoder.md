# 错误解码器
[English](../en/error-decoder.md) | **简体中文** | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

当 HTTP 发生请求错误（包括发生异常或者响应数据不符合预期）时，错误解码器可将 HTTP 相关信息解码到自定义异常中。

## 使用方式

在 `@RetrofitClient` 注解的 `errorDecoder()` 属性指定当前接口的错误解码器，自定义错误解码器需要实现 `ErrorDecoder` 接口。

## 关闭 ErrorDecoder

可通过配置 `retrofit.enable-error-decoder=false` 关闭 ErrorDecoder 功能。

---

[上一节：熔断降级](degrade.md) | [下一节：指标监控（Micrometer）](metrics.md)