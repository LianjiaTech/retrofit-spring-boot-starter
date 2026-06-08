# Log Kaydi
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | **Türkçe** | [Русский](../ru/logging.md)

Bilesen global log kaydini ve bildirimsel log kaydini destekler.

## Global Log Kaydi

Global log kaydi varsayilan olarak kapalidir (`enable=false`), aktif olarak acilmasi gerekir. Acildiktan sonra varsayilan olarak `BASIC` stratejisine gore yalnizca istek/yanit satiri (durum kodu ve gecen sure dahil) yazdirilir, masraf ihmal edilebilir. Varsayilan yapilandirma asagidadir:

```yaml
retrofit:
  global-log:
    # Log kaydini etkinlestir (varsayilan false)
    enable: false
    # Global log kaydi seviyesi
    log-level: info
    # Global log kaydi stratejisi (varsayilan BASIC, yalnizca istek/yanit satiri yazdirilir)
    log-strategy: basic
    # Istek loglarini toplu yazdirma
    aggregate: true
    # Log adi, varsayilan LoggingInterceptor'in tam sinif adi
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Logda gizlenmesi gereken hassas istek basliklari
    # Varsayilan gizlenenler: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Not: Kullanici bu ogeyi yapilandirdiginda varsayilan degerlerin tamamini override eder, gizlenmesi gereken ogeleri kendisi dahil etmelidir
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

Dort log kaydi stratejisi anlamli asagidadir:

1. **NONE**: Log yazdirma
2. **BASIC**: Yalnizca istek ve yanit satiri yazdirilir
3. **HEADERS**: Istek ve yanit satiri ile istek basliklari/yanit basliklari yazdirilir
4. **BODY**: Istek ve yanit satiri, istek basliklari/yanit basliklari ve istek govdesi/yanit govdesi (varsa) yazdirilir

## Bildirimsel Log Kaydi

Yalnizca belirli istekler icin log yazdirilmasi gerektiginde, ilgili arayuz veya yontemde `@Logging` ek aciklamasi kullanilir.

## Ozel Genisletme

Log kaydi davranisinin degistirilmesi gerektiginde, `LoggingInterceptor` sinifini devralip Spring Bean olarak yapilandirabilirsiniz.

---

[Onceki: Yontem Duzeyi Zaman Asimi Yapilandirmasi](timeout.md) | [Sonraki: Istek Yeniden Deneme](retry.md)