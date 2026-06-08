# Kesistiriciler
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | **Türkçe** | [Русский](../ru/interceptor.md)

Bu bilesen dort kesistirici mekanizmasi sunar, farkli senaryolardaki HTTP istek kesistirme ihtiyaclarini karsilar.

## Global Uygulama Kesistiricisi

Tum sistemin HTTP istekleri icin birlesik kesistirme islemi gerektiginde, `GlobalInterceptor` arayuzunu gerceklestirip Spring Bean olarak yapilandirin:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response basligina global ekle
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Global Ag Kesistiricisi

`NetworkInterceptor` arayuzunu gerceklestirip Spring Bean olarak yapilandirin.

## Bildirimsel Yol Eslestirme Kesistiricisi

Birçok senaryoda, yalnizca belirli HTTP arayuzleri icin ozel mantik gereklidir. Bu durumda yol eslestirme kesistiricisi kullanarak bu islevi zarif bir sekilde gerceklestirebilirsiniz.

### BasePathMatchInterceptor Devralarak Kesistirici Isleyici Yazma

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response basligina path.match ekle
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Arayuzde @Intercept ile Isaretleme

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// Birden fazla yol eslestirme kesistiricisi gerektiginde, @Intercut eklemeye devam edin
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

Yukaridaki `@Intercept` yapilandirmasi: `InterceptorUserService` arayuzu altinda `/api/user/**` yolu (exclude `/api/user/getUser`) altindaki istekleri kesistirir, kesistirici isleyici `PathMatchInterceptor` kullanilir. Birden fazla kesistirici gerektiginde, arayuzde birden fazla `@Intercept` ek aciklamasi isaretlenebilir.

## Ozel Kesistirici Ek Aciklamasi

Bazen "kesistirici ek aciklamasinda" bazi parametreleri dinamik olarak gecirmek ve kesistirirken bu parametreleri kullanmak gerekir. Bu durumda "ozel kesistirici ek aciklamasi" kullanilabilir, adimlar asagidadir:

1. Ozel ek aciklama tanimla, `@InterceptMark` ile isaretlenmeli ve ek aciklama `include`, `exclude`, `handler` alanlari icermelidir
2. `BasePathMatchInterceptor` devralarak kesistirici isleyici yaz
3. Arayuzde ozel ek aciklama kullan

Asagida "istek basliginda dinamik olarak `accessKeyId`, `accessKeySecret` imza bilgisi ekleme" ornegini kullanarak tam akis gosterilmektedir.

### Ozel @Sign Ek Aciklamasi Tanimlama

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {

    String accessKeyId();

    String accessKeySecret();

    String[] include() default {"/**"};

    String[] exclude() default {};

    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

`@Sign` ek aciklamasinda kullanilan kesistirici `SignInterceptor` olarak belirtilmistir.

### SignInterceptor Gerceklestirme

```java
@Component
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
        Response response = chain.proceed(newReq);
        return response.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
    }
}
```

> Not: `accessKeyId` ve `accessKeySecret` alanlari `setter` yontemi sunmalidir.

Kesistiricinin `accessKeyId` ve `accessKeySecret` alan degerleri, `@Sign` ek aciklamasinin `accessKeyId()` ve `accessKeySecret()` degerlerine gore otomatik olarak enjekte edilir. `@Sign` yer tutucu formunda bir String belirtilirse, yapilandirma ozellik degeri alinir ve enjekte edilir.

### Arayuzde @Sign Kullanma

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[Onceki: Istek Yeniden Deneme](retry.md) | [Sonraki: Devre Kesici / Dusurme](degrade.md)