# データコンバータのカスタマイズ
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | **日本語** | [한국어](../ko/converter.md) | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit は `Converter` を使用して `@Body` アノテーションのオブジェクトを HTTP リクエストボディに変換し、HTTP レスポンスボディを Java オブジェクトに変換します。以下の Converter をサポートしています：

- [Gson](https://github.com/google/gson)：com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson)：com.squareup.retrofit2:converter-jackson
- Jackson 3：com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory
- [Moshi](https://github.com/square/moshi/)：com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/)：com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire)：com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/)：com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html)：com.squareup.retrofit2:converter-jaxb
- FastJson：com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## グローバル設定

コンポーネントは `retrofit.global-converter-factories` 設定でグローバル `Converter.Factory` をサポートしています。デフォルトは `retrofit2.converter.jackson.JacksonConverterFactory` です：

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

Jackson 設定を変更する必要がある場合は、`JacksonConverterFactory` の Bean 設定をオーバーライドしてください。

## インターフェースレベルの設定

各 Java インターフェースでは、`@RetrofitClient.converterFactories` で現在のインターフェースに使用する `Converter.Factory` を指定できます。

## デフォルト Scalars Converter（String / 基本型）

コンポーネントはデフォルトで最優先（先頭）に `ScalarsConverterFactory`（`com.squareup.retrofit2:converter-scalars` 由来）を内蔵し、`String` および基本型（`Integer`/`Long`/`Boolean`/`Double` など）の戻り値をレスポンスボディの生テキストとして返します（JSON Converter を経由しません）。POJO は引き続き `global-converter-factories` で設定したコンバータ（デフォルトは Jackson）でデシリアライズされます。

このデフォルト動作は設定で無効化できます（無効化すると `String` 戻り値は JSON Converter でのデシリアライズに戻ります）:

```yaml
retrofit:
  enable-scalars-converter: false
```

> 旧バージョンの `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` は削除され、その役割はデフォルトで内蔵される `ScalarsConverterFactory` に引き継がれました。`@RetrofitClient.converterFactories` やグローバル設定で明示的にこのクラスを参照していた場合は、その参照を削除してください。

---

[前節：HTTP レスポンス結果の自動適応](response-adaptation.md) | [次節：OkHttpClient と Call.Factory SPI のカスタマイズ](okhttp-client.md)