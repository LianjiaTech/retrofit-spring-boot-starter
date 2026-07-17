# 自定义数据转换器
[English](../en/converter.md) | **简体中文** | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit 使用 `Converter` 将 `@Body` 注解的对象转换成 HTTP 请求体，将 HTTP 响应体转换成 Java 对象。支持以下几种 Converter：

- [Gson](https://github.com/google/gson)：com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson)：com.squareup.retrofit2:converter-jackson
- Jackson 3：com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory
- [Moshi](https://github.com/square/moshi/)：com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/)：com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire)：com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/)：com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html)：com.squareup.retrofit2:converter-jaxb
- FastJson：com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## 全局配置

组件支持通过 `retrofit.global-converter-factories` 配置全局 `Converter.Factory`，默认为 `retrofit2.converter.jackson.JacksonConverterFactory`：

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

如果需要修改 Jackson 配置，自行覆盖 `JacksonConverterFactory` 的 bean 配置即可。

## 接口级配置

针对每个 Java 接口，可以通过 `@RetrofitClient.converterFactories` 指定当前接口采用的 `Converter.Factory`。

## 默认 Scalars Converter（String / 基本类型）

组件默认最前置内置 `ScalarsConverterFactory`（来自 `com.squareup.retrofit2:converter-scalars`），将 `String` 与基本类型（`Integer`/`Long`/`Boolean`/`Double` 等）返回值按响应体原始文本返回，不经过 JSON Converter；POJO 仍由 `global-converter-factories` 配置的转换器（默认 Jackson）反序列化。

可通过配置项关闭该默认行为（关闭后 `String` 返回值回退到经 JSON Converter 反序列化）：

```yaml
retrofit:
  enable-scalars-converter: false
```

> 旧版本提供的 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` 已移除，其职责由默认内置的 `ScalarsConverterFactory` 取代。如曾显式在 `@RetrofitClient.converterFactories` 或全局配置中引用此类，请移除该引用。

---

[上一节：HTTP 响应结果自动适配](response-adaptation.md) | [下一节：自定义 OkHttpClient 与 Call.Factory SPI](okhttp-client.md)