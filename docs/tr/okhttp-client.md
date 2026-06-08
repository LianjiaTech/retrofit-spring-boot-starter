# OkHttpClient ve Call.Factory SPI
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | **Türkçe** | [Русский](../ru/okhttp-client.md)

Bu bilesen her `@RetrofitClient` arayuzu icin yapilandirilmis bir `OkHttpClient` (tum kesistiriciler, zaman asimi, baglantı havuzu vb. dahil) olusturur ve Retrofit'in `Call.Factory` olarak kullanir. Asagida iki ozellestirme yontemi tanitilmaktadir:

## OkHttpClient Ozellestirme

Zaman asimi ile ilgili yapilandirmalar icin, yapilandirma dosyasi veya `@Timeout` ek aciklamasi ile ayarlanabilir (bkz. [Zaman Asimi Yapilandirmasi](timeout.md)). Ancak daha esnek ve karmasik OkHttpClient yapilandirmasi gerektiginde, ozel OkHttpClient gerceklestirimi onerilir.

### SourceOkHttpClientRegistrar Arayuzunu Gerceklestirme

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // customOkHttpClient kaydet, zaman asimi 1s olarak ayarla
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### Arayuzun Kullanacagi OkHttpClient'i Belirleme

`@RetrofitClient.sourceOkHttpClient` ile mevcut arayuzun kullanacagi OkHttpClient'i belirleyin:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Call.Factory SPI Ozellestirme

Call olusturma duzeyinde ozellestirme gerektiginde (ornegin dinamik callTimeout, istek duzeyinde ozellestirme vb.), `CallFactoryConfigurer` SPI gerceklestirimi ile yapilandirabilirsiniz.

> **SPI neden gerekli?** OkHttp'in `callTimeout` tum cagrinin bitis suresidir, kesistiricide guvenilir bir sekilde override edilemez (OkHttp, kesistirici zinciri calismadan once zaman asimi planlamasini tamamlar). `CallFactoryConfigurer` Call olusturma duzeyinde mudahale eder, `OkHttpClient.newBuilder()` ile hafif client turetir (connectionPool ve dispatcher paylasilir) ve istek duzeyinde override gerceklestirir.

### CallFactoryConfigurer Arayuzunu Gerceklestirme

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
                        // newBuilder() connectionPool/dispatcher/interceptors paylasir, yalnizca callTimeout farklidir
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // Override yok -> @Timeout veya global varsayilan deger kullanilir
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Yalnizca Belirli Arayuzlerde Etkin Olma

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
        // Diger arayuzlerde dogrudan baseClient dondurulur, varsayilan davranisla esittir
        return baseClient;
    }
}
```

> `CallFactoryConfigurer` Bean kaydedilmediginde, bilesen davranisi tamamen degismez. `CallFactoryConfigurer` dondurdugu deger `OkHttpClient` olmadiginda, yontem duzeyi `@Timeout` etkin olmaz -- kullanici ozel gerceklestiriminde zaman asimini kendisi yonetmelidir.

---

[Onceki: Veri Donusturucu](converter.md) | [Sonraki: Yontem Duzeyi Zaman Asimi Yapilandirmasi](timeout.md)