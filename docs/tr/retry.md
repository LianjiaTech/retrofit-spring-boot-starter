# Istek Yeniden Deneme
[English](../en/retry.md) | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | **Türkçe** | [Русский](../ru/retry.md)

Bilesen global yeniden deneme ve bildirimsel yeniden deneme destekler.

## Global Yeniden Deneme

Global yeniden deneme varsayilan olarak kapalidir, varsayilan yapilandirma ogeleri asagidadir:

```yaml
retrofit:
  global-retry:
    # Global yeniden deneme etkin mi
    enable: false
    # Global yeniden deneme temel aralik suresi (milisaniye)
    interval-ms: 100
    # Global maksimum yeniden deneme sayisi
    max-retries: 2
    # Geri donme stratejisi: FIXED (sabit aralik, varsayilan) / EXPONENTIAL (ustel geri donme)
    backoff-strategy: fixed
    # Ustel geri donme aralik ust siniri (milisaniye), yalnizca EXPONENTIAL etkin
    max-interval-ms: 30000
    # Titresim katsayisi [0.0, 1.0], 0.0 titresim yok
    jitter: 0.0
    # Global yeniden deneme kurallari
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### Yeniden Deneme Kurallari

Yeniden deneme kurallari uc yapilandirma destekler:

1. **RESPONSE_STATUS_NOT_2XX**: Yanit durum kodu 2xx degilse yeniden deneme gerceklestirilir
2. **OCCUR_IO_EXCEPTION**: IO istisnasi olustugunda yeniden deneme gerceklestirilir
3. **OCCUR_EXCEPTION**: Herhangi bir istisna olustugunda yeniden deneme gerceklestirilir

### Geri Donme Stratejisi ve Titresim

`backoffStrategy` yeniden deneme araliginin buyume yontemini kontrol eder, varsayilan `FIXED` gecmis davranisla uyumludur:

- **FIXED**: Her yeniden deneme araligi `intervalMs` olarak sabit
- **EXPONENTIAL**: Ustel geri donme, N. yeniden deneme araligi = `intervalMs * 2^N` (N 0'dan baslar), `maxIntervalMs` ile sinirlanir, araligin sonsuz buyumesi onlenir

`jitter` (deger `[0.0, 1.0]`, varsayilan `0.0` titresim yok), hesaplanan gecikme uzerine rastgele titresim eklemek icin kullanilir, birden fazla istemcinin senkron yeniden deneme sonucunda olusan yigilma etkisini onler:

> Gecikme gecikmesi = hesaplanan gecikme * (1 + jitter * random), random `[0, 1)` araliginda rastgele bir sayidir

### Kosullu Tetikleme: Durum Kodu / Istisna Turu

`RetryRule` buyuk taneli kurallar temelinde, tetikleme kosullari daha da daraltilabilir (varsayilan bos, gecmis davranisla uyumlu):

- `retryStatusCodes`: Yalnizca yanit durum kodu listede eslesirse yeniden deneme gerceklestirilir (`RESPONSE_STATUS_NOT_2XX` kurali ile birlikte kullanilmalidir). Ornegin `{502, 503, 504}`
- `retryExceptionClasses`: Yalnizca istisna turu listede eslesirse yeniden deneme gerceklestirilir (`RetryRule` ile eslesen istisnalar temelinde daha da daraltilir). Ornegin `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Bildirimsel Yeniden Deneme

Yalnizca belirli istekler icin yeniden deneme gerektiginde, ilgili arayuz veya yontemde `@Retry` ek aciklamasi kullanilir.

## Ozel Genisletme

Istek yeniden deneme davranisinin degistirilmesi gerektiginde, `RetryInterceptor` sinifini devralip Spring Bean olarak yapilandirabilirsiniz.

---

[Onceki: Log Kaydi](logging.md) | [Sonraki: Kesistiriciler](interceptor.md)