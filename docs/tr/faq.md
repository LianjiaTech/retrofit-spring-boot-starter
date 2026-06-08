# Sikca Sorulan Sorular
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | **Türkçe** | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration Otomatik Yukleme Yapilamiyor

Bazi senaryolarda (ornegin `@SpringBootApplication(exclude = ...)` veya karisik XML yapilandirma projesi kullanildiginda), `RetrofitAutoConfiguration` normal olarak Yuklenemeyebilir. Bu durumda manuel yapilandirma icagrisi yapilabilir:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Proje hala Spring XML yapilandirma dosyalarini kullanirsa, XML yapilandirma dosyasina Spring Boot otomatik yapilandirma sinifini eklemeniz gerekir:

```xml
<!-- Spring Boot otomatik yapilandirma sinifini ice aktar -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot Yapilandirma Dosyasi Etkin Olmuyor

`application.yml` veya `application.properties` yapilandirmasi etkin olmazsa, `RetrofitProperties` Bean manuel olarak yapilandirilabilir:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties her yapilandirma degerini manuel olarak degistir
    return retrofitProperties;
}
```

## Ozellik Adi path-math ile Sinif Adi PathMatchInterceptor Farki

Yapilandirma ozelligi `auto-set-prototype-scope-for-path-math-interceptor`'daki `path-math` tarihsel adlandirmadir, ilgili kesistirici sinif adi `PathMatchInterceptor` (`match` kullanir). Bu bilinen tarihsel adlandirma farki, islev kullanimini etkilemez.

## RetrofitClient Tarama Yolu Manuel Belirleme

Varsayilan olarak, bilesen Spring Boot tarama yolunu otomatik olarak kullanarak `RetrofitClient` kaydeder. Tarama yolunun manuel olarak belirlenmesi gerektiginde, yapilandirma sinifinda `@RetrofitScan` ek aciklamasi ekleyebilirsiniz.

## Jackson Seri Hale Getirme Yapilandirmasinin Degistirilmesi

Jackson seri hale getirme / seri hale donusum davranisinin ozellestirilmesi gerektiginde, `JacksonConverterFactory` Spring Bean yapilandirmasini override edin. Bilesen varsayilan olarak `retrofit2.converter.jackson.JacksonConverterFactory` kullanir, Bean olarak kaydedildikten sonra ozel Jackson `ObjectMapper` yapilandirmasi otomatik olarak uygulanir.

---

[Ozellik Dizini](../../README.md)