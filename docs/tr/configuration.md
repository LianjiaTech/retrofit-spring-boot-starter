# Yapilandirma Ozellikleri Basvurusu
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | **Türkçe** | [Русский](../ru/configuration.md)

Bilesen birden fazla yapilandirilabilir ozellik destekler, farkli is senaryolarini karsilar. Tum yapilandirma ozellikleri ve varsayilan degerler asagidadir:

```yaml
retrofit:
  # Global donusturucu fabrikasi (varsayilan JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Global uyarlama fabrikasi (bilesen genisletmeli CallAdapterFactory dahili olarak yerlesiktir, tekrar yapilandirmayin)
  global-call-adapter-factories:
    # ...

  # Global log kaydi yapilandirmasi
  global-log:
    # Log kaydini etkinlestir (varsayilan false)
    enable: false
    # Global log kaydi seviyesi
    log-level: info
    # Global log kaydi stratejisi (varsayilan BASIC)
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

  # Global yeniden deneme yapilandirmasi
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

  # Global zaman asimi yapilandirmasi
  global-timeout:
    # Global okuma zaman asimi (milisaniye)
    read-timeout-ms: 10000
    # Global yazma zaman asimi (milisaniye)
    write-timeout-ms: 10000
    # Global baglanti zaman asimi (milisaniye)
    connect-timeout-ms: 10000
    # Global tam cagri zaman asimi (milisaniye), 0 zaman asimi yok
    call-timeout-ms: 0

  # Global baglanti havuzu yapilandirmasi
  global-connection-pool:
    # Maksimum bos baglanti sayisi
    max-idle-connections: 5
    # Baglanti yasam suresi (milisaniye)
    keep-alive-duration-ms: 300000

  # Metrik izleme yapilandirmasi (varsayilan kapali; acik olarak enable=true ayarlanmasi gerekir ve konteynerde MeterRegistry olmalidir)
  metrics:
    # Etkin mi, varsayilan false
    enable: false
    # Timer percentile
    percentiles: [0.5, 0.95, 0.99]
    # SLO histogram gruplama
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # host etiketi dahil mi
      host: false
      # uri etiketi dahil mi
      uri: true
    # Global ek etiketler
    extra-tags:
      app: my-service
    # Metrik adi on eki
    metric-name-prefix: retrofit.client

  # Devre kesici dusurme yapilandirmasi
  degrade:
    # Devre kesici dusurme turu. Varsayilan none, devre kesici dusurme etkin degil
    degrade-type: none
    # Global Sentinel dusurme yapilandirmasi
    global-sentinel-degrade:
      # Etkin mi
      enable: false
      rules:
        # Dusurme stratejisi (0: ortalama yanit suresi; 1: istisna orani; 2: istisna sayisi)
        - grade: 0
          # Her dusurme stratejisi icin esik deger. Ortalama yanit suresi(ms), istisna orani(0-1), istisna sayisi(1-N)
          count: 1000
          # Devre kesici suresi, saniye birimi
          time-window: 5
          # (Etkin istatistik zaman araliginda) devre kesiciyi tetikleyebilen minimum istek sayisi
          min-request-amount: 5
          # RT modunda yavas istek orani esik degeri
          slow-ratio-threshold: 1.0
          # Zaman araligi istatistik suresi, milisaniye birimi
          stat-interval-ms: 1000
    # Global Resilience4j dusurme yapilandirmasi
    global-resilience4j-degrade:
      # Etkin mi
      enable: false
      # Bu ad ile CircuitBreakerConfigRegistry'den CircuitBreakerConfig alinir, global devre kesici yapilandirmasi olarak kullanilir
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # PathMatchInterceptor scope'unu otomatik olarak prototype olarak ayarla
  auto-set-prototype-scope-for-path-math-interceptor: true
  # ErrorDecoder islevini etkinlestir mi
  enable-error-decoder: true
```

Cogu senaryoda, Spring Boot yapilandirma dosyasinda (application.yml veya application.properties) yukaridaki yapilandirmayi ekleyerek bilesen islevini ozel olarak degistirebilirsiniz. Yapilandirma etkin olmaz gibi sorunlar icin bkz. [Sikca Sorulan Sorular](faq.md).

**Spring Boot yapilandirma dosyasi etkin olmazsa, RetrofitProperties Bean manuel olarak yapilandirilabilir**, kod asagidadir:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties her yapilandirma degerini manuel olarak degistir
    return retrofitProperties;
}
```

---

[Onceki: RetrofitClient Ek Aciklamasi](custom-annotation.md) | [Sonraki: Diger Ozellik Ornekleri](examples.md)