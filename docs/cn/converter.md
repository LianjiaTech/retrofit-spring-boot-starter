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

## 原始字符串结果

如果接口返回的原始结果是 String 文本，且无法用 JSON 转换器转换，可以使用 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`，该转换器会直接将结果转为 String 返回。

---

[上一节：HTTP 响应结果自动适配](response-adaptation.md) | [下一节：自定义 OkHttpClient 与 Call.Factory SPI](okhttp-client.md)