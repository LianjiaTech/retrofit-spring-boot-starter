# Actuator Endpoint (RetrofitClient Metaverisi Aciga Cikarma)
[English](../en/actuator.md) | [简体中文](../cn/actuator.md) | [繁體中文](../tw/actuator.md) | [日本語](../ja/actuator.md) | [한국어](../ko/actuator.md) | [Español](../es/actuator.md) | **Türkçe** | [Русский](../ru/actuator.md)

Bilesen Spring Boot Actuator tabanli bir salt-okunur Endpoint sunar, `/actuator/retrofit` ile uygulamadaki tum `@RetrofitClient` arayuzlerinin tam yapilandirma metaverisini aciga cikarir, "belirli bir arayuzde gecerli olan baseUrl / zaman asimi / log / yeniden deneme / devre kesici yapilandirmasinin gercekten ne oldugu" gibi sorunlarin cozumune yardimci olur.

> **Opsiyonel bagimlilik, gerektiginde etkinlestirme**: Yalnizca kullanici actuator'i dahil ettiginde Endpoint yerlestirilir (`@ConditionalOnClass`), actuator dahil edilmeyen SpringBoot 3 projeleri herhangi bir etkilenme olmadan normal baslatilir. Endpoint'in aciga cikma ve switch'i tamamen Spring Boot standart management yapilandirmasina (`@ConditionalOnAvailableEndpoint`) birakilir, ozel switch olusturulmaz.

## Etkinlestirme Yontemi

1. Actuator'i ekleyin:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. Retrofit endpoint'i aciga cikarin (varsayilan actuator yalnizca `health` aciga cikarir, acik olarak eklenmelidir):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## Erisim Yontemi

| Istek | Aciklama |
|---|---|
| `GET /actuator/retrofit` | Tum client + `global` global yapilandirma segmenti + `count` listesi |
| `GET /actuator/retrofit/{arayuz tam niteligi adi}` | Arayuz tam niteligi adi ile tek client sorgula, eslesmeyen 404 dondurur |

## Yanit Yapisi Ornegini

```json
{
  "count": 2,
  "global": {
    "enableErrorDecoder": true,
    "globalConverterFactories": ["retrofit2.converter.jackson.JacksonConverterFactory"],
    "timeout": { "connectMs": 10000, "readMs": 10000, "writeMs": 10000, "callMs": 0 },
    "connectionPool": { "maxIdleConnections": 5, "keepAliveDurationMs": 300000 },
    "log":     { "enable": false, "logLevel": "INFO", "logStrategy": "BASIC", "aggregate": true },
    "retry":   { "enable": false, "maxRetries": 2, "intervalMs": 100,
                 "backoffStrategy": "FIXED", "maxIntervalMs": 30000, "jitter": 0.0,
                 "retryStatusCodes": [], "retryExceptionClasses": [],
                 "retryRules": ["RESPONSE_STATUS_NOT_2XX", "OCCUR_IO_EXCEPTION"] },
    "degrade": { "degradeType": "none",
                 "sentinel":     { "enable": false, "ruleCount": 0 },
                 "resilience4j": { "enable": false, "circuitBreakerConfigName": "defaultCircuitBreakerConfig" } },
    "metrics": { "enable": false, "metricNamePrefix": "retrofit.client", "tagHost": false, "tagUri": true }
  },
  "clients": [{
    "beanName": "userService",
    "interfaceName": "com.example.UserService",
    "baseUrl": "${test.baseUrl}",
    "resolvedBaseUrl": "http://localhost:8080/api/user/",
    "serviceId": null,
    "path": null,
    "converterFactories": [],
    "callAdapterFactories": [],
    "fallback": null,
    "fallbackFactory": null,
    "errorDecoder": "com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder$DefaultErrorDecoder",
    "validateEagerly": false,
    "sourceOkHttpClient": null,
    "timeoutEffective": true,
    "timeout": { "connectMs": 3000, "readMs": 3000, "writeMs": 10000, "callMs": 0,
                 "inheritedFields": ["writeMs", "callMs"] },
    "pool":    { "maxIdleConnections": 5, "keepAliveDurationMs": 300000,
                 "inheritedFields": ["maxIdleConnections", "keepAliveDurationMs"] },
    "logging": { "source": "interface", "enable": true, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## Alan Semanti Aciklama

- **`resolvedBaseUrl`**: Cozulen son baseUrl. Yalnizca arayuz enjekte edildiginde (orneginme baslatma tetiklendiginde) deger vardir, aksi takdirde `null` (baseUrl yavas cozulur, tetiklenmediginde onceden cozulmez).
- **`timeout` / `pool` `inheritedFields`**: `@RetrofitClient` uzerindeki ilgili alan yapilandirmasi `-1` (varsayilan deger) oldugunda "global yapilandirma tekrar kullanilir" anlamina gelir. Endpoint gercek olusturma ile tutarlı kurallara gore `-1`'i global fallback degerine cozur ve bu alan adlarini `inheritedFields`'e kaydeder, "arayuz acik yapilandirma" mi yoksa "global devralma" mi ayirt etmeye yardimci olur.
- **`timeoutEffective`**: Arayuz `sourceOkHttpClient` ile ozel OkHttpClient belirttiginde `false` olur (zaman asimi/baglanti havuzu kaynak client tarafindan belirlenir, `timeout`/`pool` gosterilmez).
- **`logging` / `retry` `source`**:
  - `"interface"`: Arayuzda `@Logging` / `@Retry` ek aciklamasi var, diger alanlar ek aciklama genisletme degeridir;
  - `"global"`: Arayuzda ilgili ek aciklama yok, calisma zamaninda global yapilandirmaya geri doner, bu durumda degerler tekrar genisletilmez, ust `global` segmentine bakin.
  - Not: Yontem duzeyi `@Logging` / `@Retry` burada detayli gosterilmez (calisma zamaninda yontem ek aciklamasi arayuzden, arayuz globalden onceliklidir).
- **`degrade.enabled`**: `RetrofitDegrade.isEnableDegrade(arayuz)` degeri; `type` global `degrade.degrade-type` (`none` / `sentinel` / `resilience4j`).
- **`fallback` / `fallbackFactory`**: Yapilandirilmadiginda (varsayilan `void.class`) `null`.

---

[Onceki: Metrik Izleme (Micrometer)](metrics.md) | [Sonraki: GraalVM Native Image / AOT Destegi](aot.md)