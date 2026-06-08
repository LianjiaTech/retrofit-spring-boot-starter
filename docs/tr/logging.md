# Log Yazdırma
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | **Türkçe** | [Русский](../ru/logging.md)

Bileşen, global log yazdırma ve bildirimsel log yazdırma desteği sağlar.

## Global Log Yazdırma

Global log yazdırma varsayılan olarak kapalıdır (`enable=false`), aktif olarak açılması gerekir. Açıldığında varsayılan olarak `BASIC` stratejisiyle yalnızca istek/yanıt satırı yazdırılır (durum kodu ve süre dahil), performans etkisi ihmal edilebilir. Varsayılan yapılandırma aşağıdaki gibidir:

```yaml
retrofit:
  global-log:
    # Log yazdırma etkinleştirme (varsayılan false)
    enable: false
    # Global log yazdırma seviyesi
    log-level: info
    # Global log yazdırma stratejisi (varsayılan BASIC, yalnızca istek/yanıt satırı yazdırılır)
    log-strategy: basic
    # İstek logları toplu yazdırma
    aggregate: true
    # Log adı, varsayılan olarak LoggingInterceptor'ın tam sınıf adı
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Loglarda gizlenmesi gereken hassas istek başlıkları
    # Varsayılan maskelenen: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Not: Kullanıcı bu öğeyi yapılandırdığında varsayılan değer tamamen override edilir, gizlenmesi gereken öğeleri kendiniz dahil etmeniz gerekir
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

Dört log yazdırma stratejisi ve anlamları aşağıdaki gibidir:

1. **NONE**: Log yazdırılmaz
2. **BASIC**: Yalnızca istek ve yanıt satırı yazdırılır
3. **HEADERS**: İstek ve yanıt satırı ile istek başlığı/yanıt başlığı yazdırılır
4. **BODY**: İstek ve yanıt satırı, istek başlığı/yanıt başlığı ve istek gövdesi/yanıt gövdesi yazdırılır (varsa)

## Bildirimsel Log Yazdırma

Yalnızca belirli isteklerde log yazdırma gerektiğinde, ilgili arayüz veya metot üzerinde `@Logging` anotasyonu kullanılabilir.

## Özel Genişletme

Log yazdırma davranışını değiştirmek gerektiğinde, `LoggingInterceptor`'dan devralma yapılabilir ve Spring Bean olarak yapılandırılabilir.

---

[Önceki: Metot Düzeyinde Zaman Aşımı Yapılandırması](timeout.md) | [Sonraki: İstek Yeniden Deneme](retry.md)