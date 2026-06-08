# Veri Donusturucu
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | [Español](../es/converter.md) | **Türkçe** | [Русский](../ru/converter.md)

Retrofit, `Converter` kullanarak `@Body` ek aciklamasi ile isaretli nesneyi HTTP istek govdesine donusturur ve HTTP yanit govdesini Java nesnesine donusturur. Asagidaki Converter'lar desteklenmektedir:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- Jackson 3: com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## Global Yapilandirma

Bilesen, `retrofit.global-converter-factories` yapilandirmasi ile global `Converter.Factory` ayarlanmasini destekler. Varsayilan olarak `retrofit2.converter.jackson.JacksonConverterFactory` kullanilir:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

Jackson yapilandirmasinin degistirilmesi gerektiginde, `JacksonConverterFactory` bean yapilandirmasini kendiniz override edin.

## Arayuz Duzeyi Yapilandirma

Her Java arayuzu icin, `@RetrofitClient.converterFactories` ile mevcut arayuzun kullanacagi `Converter.Factory` belirleyebilirsiniz.

## Ham String Sonucu

Arayuzun dondurdugu ham sonuc String metni olup JSON donusturucu ile donusturulemiyorsa, `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` kullanabilirsiniz. Bu donusturucu sonucu dogrudan String olarak dondurur.

---

[Onceki: HTTP Yanit Sonuclari Otomatik Uyarlama](response-adaptation.md) | [Sonraki: OkHttpClient ve Call.Factory SPI](okhttp-client.md)