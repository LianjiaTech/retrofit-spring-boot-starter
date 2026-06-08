# Декодер ошибок
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | **Русский**

Когда происходит ошибка HTTP-запроса (включая возникновение исключения или ответ, не соответствующий ожиданиям), декодер ошибок может декодировать HTTP-информацию в кастомное исключение.

## Использование

Укажите декодер ошибок для текущего интерфейса через атрибут `errorDecoder()` аннотации `@RetrofitClient`. Кастомный декодер ошибок должен реализовать интерфейс `ErrorDecoder`.

## Отключение ErrorDecoder

Можно отключить функцию ErrorDecoder через конфигурацию `retrofit.enable-error-decoder=false`.

---

[Предыдущая: Circuit Breaker (предохранитель)](degrade.md) | [Следующая: Метрики (Micrometer)](metrics.md)