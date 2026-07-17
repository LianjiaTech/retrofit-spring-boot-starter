# 自訂資料轉換器
[English](../en/converter.md) | [简体中文](../cn/converter.md) | **繁體中文** | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit 使用 `Converter` 將 `@Body` 註解的物件轉換成 HTTP 請求體，將 HTTP 回應體轉換成 Java 物件。支援以下幾種 Converter：

- [Gson](https://github.com/google/gson)：com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson)：com.squareup.retrofit2:converter-jackson
- [Moshi](https://github.com/square/moshi/)：com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/)：com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire)：com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/)：com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html)：com.squareup.retrofit2:converter-jaxb
- FastJson：com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## 全域設定

元件支援透過 `retrofit.global-converter-factories` 設定全域 `Converter.Factory`，預設為 `retrofit2.converter.jackson.JacksonConverterFactory`：

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

## 介面級設定

針對每個 Java 介面，可以透過 `@RetrofitClient.converterFactories` 指定當前介面採用的 `Converter.Factory`。

## 預設 Scalars Converter（String / 基本型別）

元件預設最前置內建 `ScalarsConverterFactory`（來自 `com.squareup.retrofit2:converter-scalars`），將 `String` 與基本型別（`Integer`/`Long`/`Boolean`/`Double` 等）回傳值按響應體原始文字回傳，不經過 JSON Converter；POJO 仍由 `global-converter-factories` 配置的轉換器（預設 Jackson）反序列化。

可透過配置項關閉該預設行為（關閉後 `String` 回傳值回退到經 JSON Converter 反序列化）：

```yaml
retrofit:
  enable-scalars-converter: false
```

> 舊版本提供的 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` 已移除，其職責由預設內建的 `ScalarsConverterFactory` 取代。如曾顯式在 `@RetrofitClient.converterFactories` 或全域配置中引用此類，請移除該引用。
---

[上一節：HTTP 回應結果自動適配](response-adaptation.md) | [下一節：自訂 OkHttpClient](okhttp-client.md)