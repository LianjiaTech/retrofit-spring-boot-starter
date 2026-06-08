# Devre Kesici / Geri Dönüş
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | **Türkçe** | [Русский](../ru/degrade.md)

Devre kesici / geri dönüş varsayılan olarak kapalıdır. Şu anda **Sentinel** ve **Resilience4j** olmak üzere iki uygulama desteklenmektedir.

```yaml
retrofit:
  degrade:
    # Devre kesici / geri dönüş türü, varsayılan none etkin değil anlamına gelir
    degrade-type: sentinel
```

## Sentinel

### Bağımlılık Ekleme

Sentinel bağımlılığını manuel olarak ekleyin:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### Bildirimsel Devre Kesici

`degrade-type=sentinel` yapılandırması ile etkinleştirin, ardından ilgili arayüz veya metot üzerinde `@SentinelDegrade` anotasyonu bildirimi yapın:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = SentinelFallbackUserService.class)
@SentinelDegrade(rules = {
    @SentinelDegradeRule(grade = 0, count = 100, timeWindow = 4),
    @SentinelDegradeRule(grade = 1, count = 0.01, timeWindow = 3)
})
public interface SentinelUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @SentinelDegrade(rules = {@SentinelDegradeRule(grade = 2, count = 1, timeWindow = 6)})
    User getUser(@Query("id") Long id);
}
```

### Global Sentinel Devre Kesici / Geri Dönüş

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
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
```

## Resilience4j

### Bağımlılık Ekleme

Resilience4j bağımlılığını manuel olarak ekleyin:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### Devre Kesici Yapılandırması Kaydı

`CircuitBreakerConfigRegistrar` arayüzünü uygulayarak `CircuitBreakerConfig` kaydı yapın:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Varsayılan CircuitBreakerConfig'i değiştir
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Diğer CircuitBreakerConfig kaydı
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### Bildirimsel Devre Kesici

`degrade-type=resilience4j` yapılandırması ile etkinleştirin, ardından ilgili arayüz veya metot üzerinde `@Resilience4jDegrade` bildirimi yapın:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = Resilience4jFallbackFactory.class)
@Resilience4jDegrade(circuitBreakerConfigName = "testCircuitBreakerConfig")
public interface Resilience4jUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @Resilience4jDegrade(enable = false)
    User getUser(@Query("id") Long id);
}
```

### Global Resilience4j Devre Kesici / Geri Dönüş

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Bu ad ile CircuitBreakerConfigRegistry'den CircuitBreakerConfig alınır, global devre kesici yapılandırması olarak kullanılır
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

`circuitBreakerConfigName` ile `CircuitBreakerConfig` belirtilir, `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` veya `@Resilience4jDegrade.circuitBreakerConfigName` dahil.

## Devre Kesici / Geri Dönüş Genişletme

Diğer devre kesici / geri dönüş uygulaması kullanma gerektiğinde, `BaseRetrofitDegrade`'den devralma yapılabilir ve Spring Bean olarak yapılandırılabilir.

## Fallback ve FallbackFactory

`@RetrofitClient` üzerinde `fallback` veya `fallbackFactory` ayarlanmadığında, devre kesici tetiklendiğinde doğrudan `RetrofitBlockException` istisnası fırlatılır. Kullanıcılar, `fallback` veya `fallbackFactory` ayarlayarak devre kesici durumundaki metot dönüş değerini özelleştirebilir.

> Not: `fallback` sınıfı mevcut arayüzün uygulama sınıfı olmalıdır, `fallbackFactory` `FallbackFactory<T>` uygulama sınıfı olmalıdır, generic parametre türü mevcut arayüz türü olmalıdır. Ayrıca, `fallback` ve `fallbackFactory` örnekleri Spring Bean olarak yapılandırılmalıdır.

`fallbackFactory`, `fallback`'a göre temel fark, her devre kesici durumunun istisna nedenini (cause) algılayabilmesidir.

### Fallback Örneği

```java
@Slf4j
@Service
public class HttpDegradeFallback implements HttpDegradeApi {

    @Override
    public Result<Integer> test() {
        Result<Integer> fallback = new Result<>();
        fallback.setCode(100)
                .setMsg("fallback")
                .setBody(1000000);
        return fallback;
    }
}
```

### FallbackFactory Örneği

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("Devre kesici tetiklendi! ", cause.getMessage(), cause);
        return new HttpDegradeApi() {
            @Override
            public Result<Integer> test() {
                Result<Integer> fallback = new Result<>();
                fallback.setCode(100)
                        .setMsg("fallback")
                        .setBody(1000000);
                return fallback;
            }
        };
    }
}
```

---

[Önceki: Interceptor](interceptor.md) | [Sonraki: Hata Dekoderi](error-decoder.md)