# Error Decoder
**English** | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

When an HTTP request error occurs (including exceptions or response data not matching expectations), the error decoder can decode HTTP-related information into a custom exception.

## Usage

Specify the error decoder for the current interface via the `errorDecoder()` attribute of the `@RetrofitClient` annotation. Custom error decoders must implement the `ErrorDecoder` interface.

## Disabling ErrorDecoder

You can disable the ErrorDecoder feature by configuring `retrofit.enable-error-decoder=false`.

---

[Previous: Circuit Breaker Degradation](degrade.md) | [Next: HTTP Calls Between Microservices](microservice.md)