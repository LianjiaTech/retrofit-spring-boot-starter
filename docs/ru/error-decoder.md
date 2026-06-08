# Декодер ошибок
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | **Русский**

Когда происходит ошибка HTTP-запроса (включая возникновение исключения или данные ответа не соответствуют ожиданиям), декодер ошибок может декодировать информацию, связанную с HTTP, в пользовательское исключение.

## Способ использования

Укажите декодер ошибок текущего интерфейса в свойстве `errorDecoder()` аннотации `@RetrofitClient`. Пользовательский декодер ошибок должен реализовать интерфейс `ErrorDecoder`.

## Отключение ErrorDecoder

Функцию ErrorDecoder можно отключить через конфигурацию `retrofit.enable-error-decoder=false`.

---

Предыдущий: [Обрыв цепи/деградация](degrade.md) | Следующий: [HTTP-вызовы между микросервисами](microservice.md)