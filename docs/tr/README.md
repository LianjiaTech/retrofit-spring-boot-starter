# retrofit-spring-boot-starter

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | **Türkçe** | [Русский](../ru/README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)

**[retrofit](https://square.github.io/retrofit/) HTTP API'leri Java arayuzlerine donusturmeyi destekler. Bu bilesen Retrofit'i Spring Boot ile derinlemesine entegre eder ve Cesitli pratik islevsel iyilestirmeleri destekler.**

- **Spring Boot 3.x/4.x projeleri** icin retrofit-spring-boot-starter **4.x** surumunu kullanin
  - Spring Boot 4.x varsayilan olarak Jackson 3 kullanir, ancak bu bilesenin varsayilan Converter'i Jackson 2'dir. Bu nedenle **Spring Boot 4.x projeleri icin global Converter'i Jackson 3 olarak ayarlamaniz onerilir**
  - Yapilandirma yontemi: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x projeleri** icin [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x) surumunu kullanin. Spring Boot 1.4.2 ve ust surumleri destekler.

GitHub: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Hizli Baslangic

### Bagimlilik Ekleme

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

Cogu Spring Boot projesinde bagimliligi eklemek yeterlidir. Bagimlilik enjeksiyonu sonrasinda bilesen calismazsa, sunlari deneyebilirsiniz:

#### Manuel Otomatik Yapilandirma Icerigi

Bazi durumlarda `RetrofitAutoConfiguration` dogru bir sekilde Yuklenemeyebilir. Manuel yapilandirma icagrisi icin su kodu kullanin:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Proje hala Spring XML yapilandirma dosyalarini kullanirsa, Spring Boot otomatik yapilandirma sinifini XML dosyasina ekleyin:

```xml
<!-- Spring Boot otomatik yapilandirma sinifini ice aktar -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

### HTTP Java Arayuzu Tanimlama

**Arayuzlerde `@RetrofitClient` ek aciklamasi ile isaretlenmelidir!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * ID ile kullanici adi sorgulama
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Not: **Yontem istek yollarinda `/` ile baslamaya dikkat edin**. Retrofit icin, `baseUrl = http://localhost:8080/api/test/` oldugunda:
> - Yontem yolu `person` ise tam URL: `http://localhost:8080/api/test/person`.
> - Yontem yolu `/person` ise tam URL: `http://localhost:8080/person`.

### Enjeksiyon ve Kullanim

**Arayuzu diger Service'lere enjekte ederek kullanabilirsiniz!**

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // userService yontemlerini cagir
    }
}
```

**Varsayilan olarak, `RetrofitClient` arayuzleri Spring Boot bilesen tarama yolu ile otomatik olarak kaydedilir**. Alternatif olarak, bir yapilandirma sinifinda `@RetrofitScan` kullanarak ozel bir tarama yolu belirleyebilirsiniz.

## HTTP Istek Ek Aciklamalari

HTTP istek ile ilgili ek aciklamalar Retrofit'in yerel ek aciklamalarini kullanir. Kisa bir ozet asagida sunulmustur:

| Ek Aciklama Kategorisi | Desteklenen Ek Aciklamalar |
|-------------------------|----------------------------|
| Istek Yontemleri        | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| Istek Basliklari        | `@Header` `@HeaderMap` `@Headers` |
| Sorgu Parametreleri     | `@Query` `@QueryMap` `@QueryName` |
| Yol Parametreleri       | `@Path` |
| Form Parametreleri      | `@Field` `@FieldMap` `@FormUrlEncoded` |
| Istek Govdesi           | `@Body` |
| Dosya Yukleme           | `@Multipart` `@Part` `@PartMap` |
| URL Parametreleri       | `@Url` |

> Detaylar icin resmi belgelere bakin: [Retrofit Resmi Belgesi](https://square.github.io/retrofit/)

## Ozellik Vurgulari

- [x] [HTTP Yanit Sonuclari Otomatik Uyarlama](response-adaptation.md)
- [x] [Veri Donusturucu](converter.md)
- [x] [OkHttpClient ve Call.Factory SPI](okhttp-client.md)
- [x] [Yontem Duzeyi Zaman Asimi Yapilandirmasi](timeout.md)
- [x] [Log Kaydi](logging.md)
- [x] [Istek Yeniden Deneme](retry.md)
- [x] [Kesistiriciler](interceptor.md)
- [x] [Devre Kesici / Dusurme](degrade.md)
- [x] [Hata Kod Cozucu](error-decoder.md)
- [x] [Metrik Izleme (Micrometer)](metrics.md)
- [x] [Actuator Endpoint](actuator.md)
- [x] [GraalVM Native Image / AOT Destegi](aot.md)
- [x] [Mikro Servisler Arasi HTTP Cagrisi](microservice.md)
- [x] [RetrofitClient Ek Aciklamasi](custom-annotation.md)
- [x] [Yapilandirma Ozellikleri](configuration.md)
- [x] [Diger Ozellik Ornekleri](examples.md)
- [x] [Sikca Sorulan Sorular](faq.md)