# İstek Yeniden Deneme
[English](../en/retry.md) | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | [Español](../es/retry.md) | **Türkçe** | [Русский](../ru/retry.md)

Bileşen, global yeniden deneme ve bildirimsel yeniden deneme desteği sağlar.

## Global Yeniden Deneme

Global yeniden deneme varsayılan olarak kapalıdır. Varsayılan yapılandırma öğeleri aşağıdaki gibidir:

```yaml
retrofit:
  global-retry:
    # Global yeniden deneme etkinleştirme
    enable: false
    # Global yeniden deneme temel aralık süresi (milisaniye)
    interval-ms: 100
    # Global maksimum yeniden deneme sayısı
    max-retries: 2
    # Geri çekilme stratejisi: FIXED (sabit aralık, varsayılan) / EXPONENTIAL (exponential geri çekilme)
    backoff-strategy: fixed
    # Exponential geri çekilme aralık üst sınırı (milisaniye), yalnızca EXPONENTIAL etkili
    max-interval-ms: 30000
    # Jitter katsayısı [0.0, 1.0], 0.0 jitter yok anlamına gelir
    jitter: 0.0
    # Global yeniden deneme kuralı
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### Yeniden Deneme Kuralları

Yeniden deneme kuralları üç yapılandırma destekler:

1. **RESPONSE_STATUS_NOT_2XX**: Yanıt durum kodu 2xx olmadığında yeniden deneme yürütülür
2. **OCCUR_IO_EXCEPTION**: IO istisnası oluştuğunda yeniden deneme yürütülür
3. **OCCUR_EXCEPTION**: Herhangi bir istisna oluştuğunda yeniden deneme yürütülür

### Geri Çekilme Stratejisi ve Jitter

`backoffStrategy`, yeniden deneme aralığının büyüme yöntemini kontrol eder. Varsayılan `FIXED`, geçmiş davranışla tutarlıdır:

- **FIXED**: Her yeniden deneme aralığı `intervalMs` olarak sabit
- **EXPONENTIAL**: Exponential geri çekilme, N. yeniden deneme aralığı = `intervalMs × 2^N` (N 0'dan başlar), ve `maxIntervalMs` üst sınırı ile aralığın sonsuz büyümesi önlenir

`jitter` (değer `[0.0, 1.0]`, varsayılan `0.0` jitter yok), hesaplanan gecikme üzerine rastgele jitter eklemek için kullanılır ve birden fazla client'ın eşzamanlı yeniden deneme sonucu oluşan thundering herd etkisini önler:

> Gerçek gecikme = hesaplanan gecikme × (1 + jitter × random), burada random `[0, 1)` aralığında rastgele bir sayıdır

### Koşullu Tetikleyici: Durum Kodu / İstisna Türüne Göre

`RetryRule` kaba kuralının temelinde, tetikleyici koşullar daha da daraltılabilir (varsayılan boş, geçmiş davranışla tutarlı):

- `retryStatusCodes`: Yalnızca yanıt durum kodu listede eşleştiğinde yeniden deneme yapılır (`RESPONSE_STATUS_NOT_2XX` kuralı ile birlikte kullanılmalıdır). Örneğin `{502, 503, 504}`
- `retryExceptionClasses`: Yalnızca istisna türü listede eşleştiğinde yeniden deneme yapılır (`RetryRule` ile eşleşen istisnaların temelinde daha da daraltılır). Örneğin `{SocketTimeoutException.class}`

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

Yalnızca belirli isteklerde yeniden deneme gerektiğinde, ilgili arayüz veya metot üzerinde `@Retry` anotasyonu kullanılabilir.

## Özel Genişletme

İstek yeniden deneme davranışını değiştirmek gerektiğinde, `RetryInterceptor`'dan devralma yapılabilir ve Spring Bean olarak yapılandırılabilir.

---

[Önceki: Log Yazdırma](logging.md) | [Sonraki: Interceptor](interceptor.md)