# Decodificador de errores
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | **Español** | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

Cuando ocurre un error en la solicitud HTTP (incluyendo excepciones o datos de respuesta que no cumplen las expectativas), el decodificador de errores puede decodificar la informacion relacionada con HTTP en una excepcion personalizada.

## Modo de uso

En la propiedad `errorDecoder()` de la anotacion `@RetrofitClient`, se especifica el decodificador de errores para la interfaz actual. El decodificador de errores personalizado debe implementar la interfaz `ErrorDecoder`.

## Deshabilitar ErrorDecoder

Se puede deshabilitar la funcionalidad de ErrorDecoder configurando `retrofit.enable-error-decoder=false`.

---

[Anterior: Circuit Breaker / Degradacion](degrade.md) | [Siguiente: Monitoreo de metricas (Micrometer)](metrics.md)