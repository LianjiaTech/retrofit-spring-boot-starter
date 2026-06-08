# Sık Sorulan Sorular
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | **Türkçe** | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration Otomatik Yüklenemiyor

Bazı senaryolarda (`@SpringBootApplication(exclude = ...)` veya karma XML yapılandırması kullanan projeler gibi), `RetrofitAutoConfiguration` normal olarak yüklenemeyebilir. Bu durumda manuel yapılandırma import yapılabilir:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Proje hala Spring XML yapılandırma dosyası kullanıyorsa, XML yapılandırma dosyasında Spring Boot otomatik yapılandırma sınıfı eklenmesi gerekir:

```xml
<!-- Spring Boot otomatik yapılandırma sınıfı import -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot Yapılandırma Dosyası Etkili Olmuyor

`application.yml` veya `application.properties` içindeki yapılandırma etkili olmuyorsa, `RetrofitProperties` Bean manuel olarak yapılandırılabilir:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties her yapılandırma değerini manuel olarak değiştir
    return retrofitProperties;
}
```

## path-math Özellik Adı ve PathMatchInterceptor Sınıf Adı Farkı

Yapılandırma özelliği `auto-set-prototype-scope-for-path-math-interceptor` içindeki `path-math` tarihsel adlandırmadır, ilgili interceptor sınıf adı `PathMatchInterceptor` (match kullanır). Bu bilinen tarihsel adlandırma farkıdır ve işlev kullanımını etkilemez.

## RetrofitClient Tarama Yolu Manuel Belirtme

Varsayılan olarak, bileşen Spring Boot tarama yolunu otomatik olarak kullanarak `RetrofitClient` kaydı yapar. Tarama yolunu manuel olarak belirtme gerektiğinde, yapılandırma sınıfı üzerinde `@RetrofitScan` anotasyonu eklenir.

## Jackson Serileştirme Yapılandırmasını Değiştirme

Jackson serileştirme / serileştirme kaldırma davranışını özelleştirme gerektiğinde, `JacksonConverterFactory` Spring Bean yapılandırmasını doğrudan override edin. Bileşen varsayılan olarak `retrofit2.converter.jackson.JacksonConverterFactory` kullanır, Bean olarak kayıt yapıldıktan sonra özel Jackson `ObjectMapper` yapılandırması otomatik olarak uygulanır.

---

[Özellik dizinine dön](../../README.md)