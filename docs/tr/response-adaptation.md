# HTTP Yanıt Sonucu Otomatik Adaptasyonu
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | **Türkçe** | [Русский](../ru/response-adaptation.md)

Bu bileşen, HTTP yanıt sonuçlarını Java arayüzünde tanımlanan dönüş türüne otomatik olarak adaptasyon yapar. Şu dönüş türleri desteklenmektedir:

- `Call<T>`: Adaptasyon işlemi yapılmaz, `Call<T>` nesnesi doğrudan döndürülür
- `String`: Response Body'yi `String` olarak adaptasyon yaparak döndürür
  - Varsayılan olarak JSON Converter kullanarak Response Body'nin bytes değerini String'e dönüştürür; Response Body'den doğrudan dönüştürülen String'i elde etmek istiyorsanız, `Converter.Factory` değerini `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` olarak belirleyebilirsiniz
- Temel türler (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Response Body'yi ilgili temel türde adaptasyon yaparak döndürür
- `CompletableFuture<T>`: Response Body'yi `CompletableFuture<T>` nesnesi olarak adaptasyon yaparak döndürür
- `Void`: Dönüş türü önemli olmadığında kullanılır
- `Response<T>`: Yanıtı `Retrofit.Response<T>` nesnesi olarak adaptasyon yaparak döndürür
- `Mono<T>`: Project Reactor reaktif dönüş türü
- `Single<T>`: RxJava reaktif dönüş türü (RxJava2/RxJava3 desteklenir)
- `Completable`: RxJava reaktif dönüş türü, HTTP isteğinde yanıt gövdesi olmayan senaryolar için kullanılır (RxJava2/RxJava3 desteklenir)
- Herhangi bir POJO türü: Response Body'yi ilgili POJO nesnesi olarak adaptasyon yaparak döndürür

## Adaptasyon Uygulama Yöntemi

Retrofit, `CallAdapterFactory` aracılığıyla `Call<T>` nesnesini arayüz metodunun dönüş değer türüne adaptasyon yapar. Bu bileşen aşağıdaki `CallAdapterFactory` uygulamalarını genişletmiştir:

- **BodyCallAdapterFactory**
  - HTTP isteğini senkron olarak yürütür, yanıt gövde içeriğini metodun dönüş değer türüne adaptasyon yaparak döndürür
  - Herhangi bir metod dönüş değer türü ile kullanılabilir, önceliği en düşüktür

- **ResponseCallAdapterFactory**
  - HTTP isteğini senkron olarak yürütür, yanıt gövde içeriğini `Retrofit.Response<T>` olarak adaptasyon yaparak döndürür
  - Yalnızca metod dönüş değer türü `Retrofit.Response<T>` olduğunda etkili olur

- **Reaktif Programlamayla İlgili CallAdapterFactory**
  - `Mono<T>`, `Single<T>`, `Completable` gibi reaktif türleri destekler

`CallAdapter.Factory`'den devralma ile, HTTP yanıtından Java arayüz dönüş türüne herhangi bir adaptasyon yöntemi uygulanabilir. Bileşen, `retrofit.global-call-adapter-factories` yapılandırmasıyla global çağrı adaptasyon fabrikası yapılandırmasını destekler:

```yaml
retrofit:
  # Global adaptasyon fabrikası (bileşen tarafından genişletilen CallAdapterFactory dahili olarak yerleşiktir, tekrar yapılandırmayın)
  global-call-adapter-factories:
    # ...
```

Her Java arayüzü için, `@RetrofitClient.callAdapterFactories` ile ilgili arayüzün kullanacağı `CallAdapter.Factory` belirtilebilir.

---

[Özellik dizinine dön](../../README.md) | [Sonraki: Özel Veri Dönüştürücü](converter.md)