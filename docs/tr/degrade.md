# Devre Kesici / Dusurme
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | [Español](../es/degrade.md) | **Türkçe** | [Русский](../ru/degrade.md)

Devre kesici dusurme varsayilan olarak kapalidir, mevcut durumda **Sentinel** ve **Resilience4j** iki gerceklestirim desteklenmektedir.

```yaml
retrofit:
  degrade:
    # Devre kesici dusurme turu, varsayilan none etkin degil
    degrade-type: sentinel
```

## Sentinel

### Bagimlilik Ekleme

Sentinel bagimliligini manuel olarak ekleyin:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### Bildirimsel Devre Kesici

`degrade-type=sentinel` yapilandirarak acin, ardindan ilgili arayuz veya yontemde `@SentinelDegrade` ek aciklamasini bildirin:

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

### Global Sentinel Devre Kesici Dusurme

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
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
```

## Resilience4j

### Bagimlilik Ekleme

Resilience4j bagimliligini manuel olarak ekleyin:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### Devre Kesici Yapilandirmasi Kaydetme

`CircuitBreakerConfigRegistrar` arayuzunu gerceklestirerek `CircuitBreakerConfig` kaydedin:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Varsayilan CircuitBreakerConfig'i degistir
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Diger CircuitBreakerConfig kaydet
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

`degrade-type=resilience4j` yapilandirarak acin, ardindan ilgili arayuz veya yontemde `@Resilience4jDegrade` bildirin:

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

### Global Resilience4j Devre Kesici Dusurme

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Bu ad ile CircuitBreakerConfigRegistry'den CircuitBreakerConfig alinir, global devre kesici yapilandirmasi olarak kullanilir
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

`circuitBreakerConfigName` ile `CircuitBreakerConfig` belirleyin, `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` veya `@Resilience4jDegrade.circuitBreakerConfigName` dahil.

## Devre Kesici Dusurme Genisletme

Diger devre kesici dusurme gerceklestirimi gerektiginde, `BaseRetrofitDegrade` sinifini devralip Spring Bean olarak yapilandirin.

## Fallback ve FallbackFactory

`@RetrofitClient` `fallback` veya `fallbackFactory` ayarlanmadiginda, devre kesici tetiklendiginde dogrudan `RetrofitBlockException` istisnasi firlatilir. Kullanici `fallback` veya `fallbackFactory` ayarlayarak devre kesici tetiklendigindeki yontem donus degerini ozellestirebilir.

> Not: `fallback` sinifi mevcut arayuzun gerceklestirim sinifi olmalidir, `fallbackFactory` `FallbackFactory<T>` gerceklestirim sinifi olmalidir, generic parametre turu mevcut arayuz turudur. Ayrıca, `fallback` ve `fallbackFactory` ornekleri Spring Bean olarak yapilandirilmalidir.

`fallbackFactory` ile `fallback` arasindaki temel fark, her devre kesici tetiklemesinin istisna nedenini (cause) algilayabilmesidir.

### Fallback Ornegini

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

### FallbackFactory Ornegini

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

[Onceki: Kesistiriciler](interceptor.md) | [Sonraki: Hata Kod Cozucu](error-decoder.md)