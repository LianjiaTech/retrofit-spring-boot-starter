# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | **Türkçe** | [Русский](../ru/README.md)

**[Retrofit](https://square.github.io/retrofit/) HTTP API'lerini Java arayüzlerine dönüştürmeyi destekler. Bu bileşen, Retrofit ve Spring Boot'u derinlemesine entegre eder ve çeşitli pratik fonksiyon iyileştirmeleri sağlar.**

- **Spring Boot 3.x/4.x projeleri** için retrofit-spring-boot-starter **4.x** sürümünü kullanın
  - Spring Boot 4.x varsayılan olarak Jackson 3 kullanır, ancak bu bileşen varsayılan Converter olarak Jackson 2 kullanır, bu nedenle **4.x projelerinde global Converter'ı Jackson 3 olarak ayarlamanız önerilir**
  - Yapılandırma方式：`retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x projeleri** için retrofit-spring-boot-starter **2.x** sürümünü kullanın, Spring Boot 1.4.2 ve üzeri sürümleri destekler

> Proje sürekli optimize edilmekte ve güncellenmektedir. ISSUE ve PR göndermekten çekinmeyin! Bir star vermek, sürekli güncellemelerimize en büyük destek olacaktır！

GitHub：[https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee：[https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Hızlı Başlangıç

### Bağımlılık Ekleme

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

Bağımlılığı ekledikten sonra kullanmaya başlayabilirsiniz. Sorun yaşarsanız, [Sıkça Sorulan Sorular](faq.md) bölümüne bakın.

### HTTP Arayüz Tanımlama

**Arayüz mutlaka `@RetrofitClient` anotasyonu ile işaretlenmelidir!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * id'ye göre kullanıcı adını sorgulama
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Not：**Metot istek yollarında `/` ile başlamaya dikkat edin.** Retrofit'in yol birleştirme kuralları：`baseUrl = http://localhost:8080/api/test/` ise, metot yolu `person` tam yol `http://localhost:8080/api/test/person` olur；metot yolu `/person` ise tam yol `http://localhost:8080/person` olur.

### Enjeksiyon ile Kullanım

Arayüzü diğer Service'ler içine enjekte ederek kullanabilirsiniz：

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // userService çağırma
    }
}
```

### HTTP İstek Anotasyonları

HTTP istek ile ilgili anotasyonlar tamamen Retrofit'in orijinal anotasyonları kullanılır：

| Anotasyon Kategorisi | Desteklenen Anotasyonlar |
|----------------------|--------------------------|
| İstek yöntemi | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| İstek başlığı | `@Header` `@HeaderMap` `@Headers` |
| Query parametreleri | `@Query` `@QueryMap` `@QueryName` |
| Path parametreleri | `@Path` |
| Form parametreleri | `@Field` `@FieldMap` `@FormUrlEncoded` |
| İstek gövdesi | `@Body` |
| Dosya yükleme | `@Multipart` `@Part` `@PartMap` |
| URL parametreleri | `@Url` |

> Detaylı bilgi için [Retrofit Resmi Dokümantasyonu](https://square.github.io/retrofit/) sayfasına bakın

## Fonksiyon Özellikleri

- [x] [HTTP Yanıt Otomatik Adaptasyonu](response-adaptation.md)
- [x] [Özel Veri Dönüştürücü](converter.md)
- [x] [Özel OkHttpClient ve Call.Factory SPI](okhttp-client.md)
- [x] [Metot Seviyesi Zaman Aşımı Yapılandırması](timeout.md)
- [x] [Log Yazdırma](logging.md)
- [x] [İstek Yeniden Deneme](retry.md)
- [x] [Interceptor](interceptor.md)
- [x] [Devre Kesici / Geri Dönüş](degrade.md)
- [x] [Hata Dekoderi](error-decoder.md)
- [x] [Mikroservis Arası HTTP Çağrısı](microservice.md)
- [x] [Özel RetrofitClient Anotasyonu](custom-annotation.md)
- [x] [Global Yapılandırma Referansı](configuration.md)
- [x] [Diğer Fonksiyon Örnekleri](examples.md)
- [x] [Sıkça Sorulan Sorular](faq.md)

## Geri Bildirim

Herhangi bir sorun varsa, issue oluşturmaya veya QQ grubuna katılarak geri bildirim vermeye hoş geldiniz.

Grup numarası：806714302

![QQ群图片](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)