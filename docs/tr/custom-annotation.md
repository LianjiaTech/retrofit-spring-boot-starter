# RetrofitClient Ek Aciklamasi
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | **Türkçe** | [Русский](../ru/custom-annotation.md)

Bazen Java arayuzundeki `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` vb. ek aciklamalarinin varsayilan degerleri is gereksinimlerini karsilamaz. Bir yontem her arayuzde ilgili ek aciklama ozelliklerini degistirmektir, ancak bu birçok arayuzde ayni mantik yapilmasina yol acar, zarif degildir. Diger bir yontem ozel RetrofitClient ek aciklamasi tanimlamak, diger arayuzlerin yalnizca ozel ek aciklama kullanmasidir.

Asagida ozel ek aciklama `@MyRetrofitClient` tanimlanmis, birden fazla ek aciklamasinin varsayilan ozelliklerini bir ek aciklamada birlestirmistir:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Logging(logLevel = LogLevel.WARN)
@Retry(intervalMs = 200)
public @interface MyRetrofitClient {

    @AliasFor(annotation = RetrofitClient.class, attribute = "converterFactories")
    Class<? extends Converter.Factory>[] converterFactories() default {GsonConverterFactory.class};

    @AliasFor(annotation = Logging.class, attribute = "logStrategy")
    LogStrategy logStrategy() default LogStrategy.BODY;
}
```

---

[Onceki: Mikro Servisler Arasi HTTP Cagrisi](microservice.md) | [Sonraki: Yapilandirma Ozellikleri Basvurusu](configuration.md)