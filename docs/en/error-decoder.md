# Error Decoder
**English** | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

When an HTTP request error occurs (including exceptions or unexpected response data), the error decoder can decode HTTP-related information into a custom exception.

## Usage

Specify the error decoder for the current interface via the `errorDecoder()` attribute of the `@RetrofitClient` annotation. Custom error decoders must implement the `ErrorDecoder` interface.

## Disable ErrorDecoder

You can disable the ErrorDecoder feature by configuring `retrofit.enable-error-decoder=false`.

---

[Previous: Circuit Breaking](degrade.md) | [Next: Metrics Monitoring (Micrometer)](metrics.md)