# Tam Yapılandırma Öğeleri Referansı
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | **Türkçe** | [Русский](../ru/configuration.md)

Bileşen, farklı iş senaryolarını karşılamak için birden fazla yapılandırılabilir özellik destekler. Tüm yapılandırma özellikleri ve varsayılan değerler aşağıdaki gibidir:

```yaml
retrofit:
  # Global dönüştürücü fabrikası (varsayılan JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Global adaptasyon fabrikası (bileşen tarafından genişletilen CallAdapterFactory dahili olarak yerleşiktir, tekrar yapılandırmayın)
  global-call-adapter-factories:
    # ...

  # Global log yazdırma yapılandırması
  global-log:
    # Log yazdırma etkinleştirme (varsayılan false)
    enable: false
    # Global log yazdırma seviyesi
    log-level: info
    # Global log yazdırma stratejisi (varsayılan BASIC)
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

  # Global yeniden deneme yapılandırması
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

  # Global zaman aşımı yapılandırması
  global-timeout:
    # Global okuma zaman aşımı süresi (milisaniye)
    read-timeout-ms: 10000
    # Global yazma zaman aşımı süresi (milisaniye)
    write-timeout-ms: 10000
    # Global bağlantı zaman aşımı süresi (milisaniye)
    connect-timeout-ms: 10000
    # Global tam çağrı zaman aşımı süresi (milisaniye), 0 zaman aşımı yok anlamına gelir
    call-timeout-ms: 0

  # Global bağlantı havuzu yapılandırması
  global-connection-pool:
    # Maksimum boş bağlantı sayısı
    max-idle-connections: 5
    # Bağlantı süre tutma süresi (milisaniye)
    keep-alive-duration-ms: 300000

  # Devre kesici / geri dönüş yapılandırması
  degrade:
    # Devre kesici / geri dönüş türü. Varsayılan none, devre kesici / geri dönüş etkin değil
    degrade-type: none
    # Global Sentinel geri dönüş yapılandırması
    global-sentinel-degrade:
      # Etkinleştirme
      enable: false
      rules:
        # Geri dönüş stratejisi (0: ortalama yanıt süresi; 1: istisna oranı; 2: istisna sayısı)
        - grade: 0
          # Her geri dönüş stratejisi için eşik değeri. Ortalama yanıt süresi(ms), istisna oranı(0-1), istisna sayısı(1-N)
          count: 1000
          # Devre kesici süresi, saniye birimi
          time-window: 5
          # (Geçerli istatistik zaman aralığında) devre kesiciyi tetikleyebilecek minimum istek sayısı
          min-request-amount: 5
          # RT modunda yavaş istek oranı eşik değeri
          slow-ratio-threshold: 1.0
          # Zaman aralığı istatistik süresi, milisaniye birimi
          stat-interval-ms: 1000
    # Global Resilience4j geri dönüş yapılandırması
    global-resilience4j-degrade:
      # Etkinleştirme
      enable: false
      # Bu ad ile CircuitBreakerConfigRegistry'den CircuitBreakerConfig alınır, global devre kesici yapılandırması olarak kullanılır
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # PathMatchInterceptor scope otomatik prototype olarak ayarlama
  auto-set-prototype-scope-for-path-math-interceptor: true
  # ErrorDecoder işlevi etkinleştirme
  enable-error-decoder: true
```

Çoğu senaryoda, Spring Boot yapılandırma dosyasında (application.yml veya application.properties) yukarıdaki yapılandırmayı ekleyerek bileşen işlevselliğini özelleştirebilirsiniz. Yapılandırma etkili olmama gibi sorunlarla karşılaştığınızda, [Sık Sorulan Sorular](faq.md) bölümüne bakın.

---

[Önceki: Özel RetrofitClient Anotasyonu](custom-annotation.md) | [Sonraki: Diğer İşlev Örnekleri](examples.md)