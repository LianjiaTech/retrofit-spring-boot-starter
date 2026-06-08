# Hata Kod Cozucu
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | **Türkçe** | [Русский](../ru/error-decoder.md)

HTTP istek hatasi olustugunda (istisna olusumu veya yanit verilerinin beklenmedik olmasi dahil), hata kod cozucusu HTTP ile ilgili bilgileri ozel istisnaya kod cozumu yapabilir.

## Kullanim Yontemi

`@RetrofitClient` ek aciklamasinin `errorDecoder()` ozelligi ile mevcut arayuzun hata kod cozucusunu belirleyin. Ozel hata kod cozucusu `ErrorDecoder` arayuzunu gerceklestirmelidir.

## ErrorDecoder'i Kapama

`retrofit.enable-error-decoder=false` yapilandirmasi ile ErrorDecoder islevi kapali olabilir.

---

[Onceki: Devre Kesici / Dusurme](degrade.md) | [Sonraki: Metrik Izleme (Micrometer)](metrics.md)