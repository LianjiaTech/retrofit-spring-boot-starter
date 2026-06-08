# GraalVM Native Image / AOT Destegi
[English](../en/aot.md) | [简体中文](../cn/aot.md) | [繁體中文](../tw/aot.md) | [日本語](../ja/aot.md) | [한국어](../ko/aot.md) | [Español](../es/aot.md) | **Türkçe** | [Русский](../ru/aot.md)

Bilesen Spring AOT destegini yerlesik olarak sunar, Spring Boot 3.x / 4.x altinda GraalVM Native Image'e derlendiginde ** kutudan cikar kullanima hazir, `reflect-config.json` / `proxy-config.json` manuel yazim gerekmez**.

Olusturma donemi (`spring-boot:process-aot` veya native derleme) her `@RetrofitClient` arayuzu icin otomatik olarak kaydeder:

- **JDK dinamik proxy**: `Retrofit.create(arayuz)` ve devre kesici dusurme proxy'si arayuz tabanli JDK proxy olusturma;
- **Arayuz refleksiyonu**: Yontem imzasi ve parametre ek aciklamalari native altinda refleksiyon ile gorunur olmalidir, Retrofit HTTP istek cozumu icin;
- **Ek aciklama referans sinifinin refleksiyon olusturmasi**: `@RetrofitClient` uzerindeki `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory`, ve `@InterceptMark` (`@Intercept` / `@Sign` dahil) ek aciklamasinin `handler` kesistirici sinifi, calisma doneminde refleksiyon ile olusturulabilir ve ozellikler enjekte edilebilir;
- **Actuator deger nesnesi seri hale getirme**: `/actuator/retrofit` yanit sonucunun refleksiyon seri hale getirme.

> Bu yetenek `RetrofitAotProcessor` (`BeanFactoryInitializationAotProcessor`) ile gerceklestirilir, **yalnizca AOT olusturma doneminde etkilidir**, normal JVM baslangici ve native calisma doneminde herhangi bir mantik calistirmaz, islev ve performans icin sifir etki.
>
> Ozel `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` vb. JSON seri hale getirme ile karmasik is varliklarina donusturulurse, is varliklarinin native refleksiyon hints'i Spring standart yontemi ile (ornegin `@RegisterReflectionForBinding`) bildirilmelidir -- bu belirli is modeliyle ilgili olup bilesen gorev kapsaminda degildir.

---

[Onceki: Actuator Endpoint](actuator.md) | [Sonraki: Mikro Servisler Arasi HTTP Cagrisi](microservice.md)