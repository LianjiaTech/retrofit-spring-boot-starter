# Custom Data Converters
**English** | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit uses `Converter` to convert `@Body`-annotated objects into HTTP request bodies, and to convert HTTP response bodies into Java objects. The following converters are supported:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- Jackson 3: com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## Global Configuration

The component supports configuring global `Converter.Factory` via `retrofit.global-converter-factories`. The default is `retrofit2.converter.jackson.JacksonConverterFactory`:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

To customize Jackson configuration, simply override the `JacksonConverterFactory` bean configuration.

## Interface-level Configuration

For each Java interface, you can specify the `Converter.Factory` to use via `@RetrofitClient.converterFactories`.

## Raw String Result

If the interface returns raw String text that cannot be converted by a JSON converter, you can use `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`, which directly converts the result to String and returns it.

---

[Previous: HTTP Response Adaptation](response-adaptation.md) | [Next: Custom OkHttpClient & Call.Factory SPI](okhttp-client.md)