# Özel Veri Dönüştürücü
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | [Español](../es/converter.md) | **Türkçe** | [Русский](../ru/converter.md)

Retrofit, `Converter` kullanarak `@Body` anotasyonu ile işaretlenmiş nesneyi HTTP istek gövdesine dönüştürür ve HTTP yanıt gövdesini Java nesnesine dönüştürür. Aşağıdaki Converter türleri desteklenmektedir:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## Global Yapılandırma

Bileşen, `retrofit.global-converter-factories` yapılandırmasıyla global `Converter.Factory` yapılandırmasını destekler. Varsayılan değer `retrofit2.converter.jackson.JacksonConverterFactory`:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

## Arayüz Düzeyinde Yapılandırma

Her Java arayüzü için, `@RetrofitClient.converterFactories` ile ilgili arayüzün kullanacağı `Converter.Factory` belirtilebilir.

## Ham String Sonucu

Arayüzün döndürdüğü ham sonuç String metni ise ve JSON dönüştürücü ile dönüştürülemiyorsa, `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` kullanılabilir. Bu dönüştürücü, sonucu doğrudan String olarak döndürür.

---

[Önceki: HTTP Yanıt Sonucu Otomatik Adaptasyonu](response-adaptation.md) | [Sonraki: Özel OkHttpClient](okhttp-client.md)