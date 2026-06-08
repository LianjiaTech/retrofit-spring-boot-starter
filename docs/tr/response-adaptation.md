# HTTP Yanit Sonuclari Otomatik Uyarlama
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | **Türkçe** | [Русский](../ru/response-adaptation.md)

Bu bilesen HTTP yanit sonuclarini Java arayuzunde tanimlanan donus turune otomatik olarak uyarlar. Su anda asagidaki donus turleri desteklenmektedir:

- `Call<T>`: Uyarlama islemi yapilmaz, dogrudan `Call<T>` nesnesi dondurulur
- `String`: Yanit Govdesi `String` olarak uyarlanir ve dondurulur
  - Varsayilan olarak JSON Converter kullanilarak Yanit Govdesinin bytes degeri String'e donusturulur; Yanit Govdesini dogrudan String olarak almak istiyorsaniz, `Converter.Factory` olarak `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` belirleyebilirsiniz
- Temel turler (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Yanit Govdesi ilgili temel ture uyarlanir
- `CompletableFuture<T>`: Yanit Govdesi `CompletableFuture<T>` nesnesi olarak uyarlanir ve dondurulur
- `Void`: Donus turu onemsiz oldugunda kullanilir
- `Response<T>`: Yanit `Retrofit.Response<T>` nesnesi olarak uyarlanir ve dondurulur
- `Mono<T>`: Project Reactor reaktif donus turu
- `Single<T>`: RxJava reaktif donus turu (RxJava2/RxJava3 destekler)
- `Completable`: RxJava reaktif donus turu, HTTP isteginde yanit govdesi olmayan senaryolar icin (RxJava2/RxJava3 destekler)
- Herhangi bir POJO turu: Yanit Govdesi ilgili POJO nesnesine uyarlanir ve dondurulur

## Uyarlama Gerceklestirme Yontemi

Retrofit, `CallAdapterFactory` ile `Call<T>` nesnesini arayuz yonteminin donus degeri turune uyarlar. Bu bilesen asagidaki `CallAdapterFactory` gerceklestirimlerini genisletir:

- **BodyCallAdapterFactory**
  - HTTP istegini senkron olarak gerceklestirir, yanit govdesi icergini yontemin donus degeri turune uyarlar
  - Herhangi bir yontem donus degeri turu icin kullanilabilir, onceligi en dusuktur

- **ResponseCallAdapterFactory**
  - HTTP istegini senkron olarak gerceklestirir, yanit govdesi icergini `Retrofit.Response<T>` olarak uyarlar
  - Yalnizca yontem donus degeri turu `Retrofit.Response<T>` oldugunda etkin olur

- **Reaktif Programlama ile ilgili CallAdapterFactory**
  - `Mono<T>`, `Single<T>`, `Completable` gibi reaktif turleri destekler

`CallAdapter.Factory` sinifini devralarak, HTTP yanitindan Java arayuzu donus turune herhangi bir uyarlama gerceklestirilebilir. Bilesen, `retrofit.global-call-adapter-factories` yapilandirmasi ile global cagri uyarlama fabrikasinin ayarlanmasini destekler:

```yaml
retrofit:
  # Global uyarlama fabrikasi (bilesen genisletmeli CallAdapterFactory dahili olarak yerlesiktir, tekrar yapilandirmayin)
  global-call-adapter-factories:
    # ...
```

Her Java arayuzu icin, `@RetrofitClient.callAdapterFactories` ile mevcut arayuzun kullanacagi `CallAdapter.Factory` belirleyebilirsiniz.

---

[Ozellik Dizini](../../README.md) | [Sonraki: Veri Donusturucu](converter.md)