# Özel RetrofitClient Anotasyonu
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | **Türkçe** | [Русский](../ru/custom-annotation.md)

Bazen, Java arayüzündeki `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` gibi anotasyonların varsayılan değerleri iş gereksinimlerini karşılamaz. Bir yöntem, her arayüzde ilgili anotasyon özelliklerini değiştirmektir, ancak bu birçok arayüzde aynı mantığı yapma gereksinimi doğurur, zarif değildir. Diğer bir yöntem, özel RetrofitClient anotasyon tanımlama, daha sonra diğer arayüzler yalnızca özel anotasyon kullanabilir.

Aşağıda, özel anotasyon `@MyRetrofitClient` tanımlanmış, birden fazla anotasyonun varsayılan özellikleri tek anotasyon olarak birleştirilmiştir:

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

[Önceki: Mikroservisler Arası HTTP Çağrı](microservice.md) | [Sonraki: Tam Yapılandırma Öğeleri Referansı](configuration.md)