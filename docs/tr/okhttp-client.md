# Özel OkHttpClient ve Call.Factory
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | **Türkçe** | [Русский](../ru/okhttp-client.md)

Bu bileşen, her `@RetrofitClient` arayüzü için yapılandırılmış bir `OkHttpClient` oluşturur (tüm interceptorlar, zaman aşımı, bağlantı havuzu vb. dahil) ve Retrofit'in `Call.Factory` olarak kullanır. Aşağıda iki özelleştirme yöntemi tanımlanmaktadır:

## Özel OkHttpClient

Zaman aşımı ile ilgili yapılandırmalar, yapılandırma dosyası veya `@Timeout` anotasyonu ile ayarlanabilir ([Zaman aşımı yapılandırması](timeout.md) bkz.). Ancak daha esnek ve karmaşık OkHttpClient yapılandırması gerektiğinde, özel OkHttpClient uygulaması önerilir.

### SourceOkHttpClientRegistrar Arayüzünü Uygulama

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // customOkHttpClient kaydı, zaman aşımı 1s olarak ayarlandı
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### Arayüzün Kullanacağı OkHttpClient'ı Belirtme

`@RetrofitClient.sourceOkHttpClient` ile ilgili arayüzün kullanacağı OkHttpClient'ı belirleyin:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Özel Call.Factory SPI

Call oluşturma düzeyinde özelleştirme gerektiğinde (dinamik callTimeout, istek düzeyinde özelleştirme vb.), `CallFactoryConfigurer` SPI uygulaması ile gerçekleştirilebilir.

> **SPI neden gerekli?** OkHttp'in `callTimeout` değeri tüm çağrının son tarihidir ve interceptor içinde güvenilir şekilde override edilemez (OkHttp, interceptor zinciri yürütmeden önce timeout planlamasını tamamlar). `CallFactoryConfigurer`, Call oluşturma düzeyinde müdahale eder ve `OkHttpClient.newBuilder()` ile hafif client türeterek (connectionPool ve dispatcher paylaşılır) istek bazlı override gerçekleştirir.

### CallFactoryConfigurer Arayüzünü Uygulama

```java
@Component
public class DynamicCallTimeoutConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        return new Call.Factory() {
            @Override
            public Call newCall(Request request) {
                Invocation invocation = request.tag(Invocation.class);
                if (invocation != null) {
                    MyCallTimeout ann = invocation.method().getAnnotation(MyCallTimeout.class);
                    if (ann != null) {
                        // newBuilder() connectionPool/dispatcher/interceptors paylaşır, yalnızca callTimeout farklı
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // Override yok → @Timeout veya global varsayılan değer kullanılır
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Yalnızca Belirli Arayüzlerde Etkili Olma

```java
@Component
public class SelectiveCallFactoryConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        if (retrofitInterface == SlowApiService.class) {
            return baseClient.newBuilder()
                    .callTimeout(30_000, TimeUnit.MILLISECONDS)
                    .build();
        }
        // Diğer arayüzler baseClient doğrudan döndürülür, varsayılan davranışa eşdeğer
        return baseClient;
    }
}
```

> `CallFactoryConfigurer` Bean kaydı yapılmadığında, bileşen davranışı tamamen değişmez.

---

[Önceki: Özel Veri Dönüştürücü](converter.md) | [Sonraki: Metot Düzeyinde Zaman Aşımı Yapılandırması](timeout.md)