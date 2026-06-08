# Пользовательский конвертер данных
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | **Русский**

Retrofit использует `Converter` для преобразования объекта с аннотацией `@Body` в тело HTTP-запроса и тела HTTP-ответа в Java-объект. Поддерживаются следующие Converter:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## Глобальная конфигурация

Компонент поддерживает конфигурацию глобальной `Converter.Factory` через `retrofit.global-converter-factories`, по умолчанию используется `retrofit2.converter.jackson.JacksonConverterFactory`:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

## Конфигурация на уровне интерфейса

Для каждого Java-интерфейса можно указать `Converter.Factory`, используемый текущим интерфейсом, через `@RetrofitClient.converterFactories`.

## Результат в виде исходной строки

Если интерфейс возвращает исходный результат в виде String-текста, который невозможно преобразовать с помощью JSON-конвертера, можно использовать `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`. Этот конвертер напрямую преобразует результат в String и возвращает его.

---

Предыдущий: [Автоматическая адаптация результатов HTTP-ответа](response-adaptation.md) | Следующий: [Пользовательский OkHttpClient](okhttp-client.md)