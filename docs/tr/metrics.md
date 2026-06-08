# Metrik Izleme (Micrometer)
[English](../en/metrics.md) | [简体中文](../cn/metrics.md) | [繁體中文](../tw/metrics.md) | [日本語](../ja/metrics.md) | [한국어](../ko/metrics.md) | [Español](../es/metrics.md) | **Türkçe** | [Русский](../ru/metrics.md)

Bilesen [Micrometer](https://micrometer.io/) tabanlı metrik toplama yetenegi yerlesik olarak sunar. **Varsayilan olarak kapalidir**, `retrofit.metrics.enable=true` acik olarak ayarlamaniz gerekir.

> **Neden varsayilan kapali ve acik olarak acilma**: Spring Boot autoconfig'ler arasinda guvenilir yukleme sirasi kisiti yoktur, `@ConditionalOnBean(MeterRegistry.class)` ile otomatik etkinlestirme degerlendirme zamani sorunlari nedeniyle "kullanici actuator'i dahil etti ancak metrik yok" seklinde gizli basarisizliga yol acar. Opt-in olarak degistirildikten sonra davranis tamamen tahmin edilebilir: kullanici actuator'i dahil etse bile otomatik olarak metrik yerlestirilmez; acik olarak acildiginda konteynerde `MeterRegistry` yoksa, baslangic hızli basarisiz olur, sessiz metrik eksikligi yerine.

## Etkinlestirme Yontemi

1. Micrometer ve ilgili izleme arka ucunu (Prometheus / Datadog / Atlas vb.) ekleyin. Spring Boot Actuator `MeterRegistry` kaydeder:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. Yapilandirmada acik olarak acin:

```yaml
retrofit:
  metrics:
    enable: true
```

## Toplanan Metrikler

| Metrik Adi | Tur | Anlami |
|---|---|---|
| `retrofit.client.requests` | Timer | Her HTTP cagri gecen sure dagilimi (percentile ve SLO histogram dahil) |
| `retrofit.client.requests.active` | LongTaskTimer | Devam eden istek sayisi ve en uzun yasam suresi |
| `retrofit.client.errors` | Counter | Istek istisna sayisi (istisna sinif adi boyutunda) |

## Etiket Boyutlari

Varsayilan tag (kardinalite sinirli, Prometheus vb. yuksek kardinalite hassas arka uclar icin guvenle kullanilabilir):

| Tag | Anlami | Deger Ornegi |
|---|---|---|
| `client` | Retrofit arayuzunun basit sinif adi | `UserService` |
| `method` | Java yontem adi | `getUser` |
| `http.method` | HTTP yontemi | `GET`/`POST` |
| `uri` | Ek aciklamasindaki yol sablonu (`@Path` genisletilmez) | `user/{id}` |
| `status` | Durum kodu grubu | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | Is sonucu | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | Yalnizca errors metriği, istisna sinif adi | `SocketTimeoutException` |

> **Not**: tag degeri sinirli bir koleksiyon olmalidir, bu nedenle `uri` etiketi ek aciklamasindaki yol sablonunu ( `{id}` yer tutucu dahil) kullanir, genisletilmis gercek URL degil. Bu, dinamik yol parametreleri nedeniyle metrik kardinalite patlamasini onler.

## Yapilandirma Ogeleri

```yaml
retrofit:
  metrics:
    # Etkin mi, varsayilan false. Acik olarak true ayarlanmasi gerekir, metrik kesistirici yerlestirilir.
    enable: true
    # Timer yayimlanan percentile; bos array yayimlanmaz
    percentiles: [0.5, 0.95, 0.99]
    # SLO histogram gruplama; bos array histogram yayimlanmaz
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # host etiketi dahil mi, varsayilan kapali (dinamik baseUrl senaryosunda host sayisi buyuk olabilir)
      host: false
      # uri etiketi dahil mi, varsayilan acik
      uri: true
    # Global statik ek etiketler
    extra-tags:
      app: my-service
      env: prod
    # Metrik adi on eki, varsayilan retrofit.client
    metric-name-prefix: retrofit.client
```

## Ozel Etiketler

Varsayilan tag boyutu gereksinimleri karsilamiyorsa, `RetrofitTagsProvider` arayuzunu gerceklestirip Spring Bean olarak kaydedin, varsayilan gerceklestirimi otomatik olarak override eder:

```java
@Component
public class TenantAwareTagsProvider implements RetrofitTagsProvider {

    private final RetrofitTagsProvider delegate;

    public TenantAwareTagsProvider(MetricsProperty property) {
        this.delegate = new DefaultRetrofitTagsProvider(property);
    }

    @Override
    public Tags getTags(Request request, Response response, Throwable exception) {
        return delegate.getTags(request, response, exception)
                .and("tenant", TenantContext.current());
    }
}
```

> Ozel gerceklestirimde dikkat edilmelidir: tag deger koleksiyonu sinirli, tag sirasi ve adi kararli olmalidir, aksi takdirde Micrometer birden fazla anlamsiz Meter olusturur, bellek israfina yol acar.

---

[Onceki: Hata Kod Cozucu](error-decoder.md) | [Sonraki: Actuator Endpoint](actuator.md)