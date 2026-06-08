# Hata Dekoderi
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | **Türkçe** | [Русский](../ru/error-decoder.md)

HTTP istek hatası oluştuğunda (istisna oluşması veya yanıt verisinin beklenen sonuçla uyuşmaması dahil), hata dekoderi HTTP ile ilgili bilgileri özel istisnaya dönüştürür.

## Kullanım Yöntemi

`@RetrofitClient` anotasyonunun `errorDecoder()` özelliği ile mevcut arayüzün hata dekoderini belirleyin. Özel hata dekoderi `ErrorDecoder` arayüzünü uygulaymalıdır.

## ErrorDecoder Kapatma

`retrofit.enable-error-decoder=false` yapılandırması ile ErrorDecoder işlevi kapatılabilir.

---

[Önceki: Devre Kesici / Geri Dönüş](degrade.md) | [Sonraki: Mikroservisler Arası HTTP Çağrı](microservice.md)