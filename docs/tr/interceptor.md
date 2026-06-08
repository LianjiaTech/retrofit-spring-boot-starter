# Interceptor
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | **Türkçe** | [Русский](../ru/interceptor.md)

Bu bileşen, farklı senaryolardaki HTTP istek拦截 gereksinimlerini karşılamak için dört interceptor mekanizması sağlar.

## Global Uygulama Interceptor

Tüm sistemin HTTP isteklerine统一拦截 işlemi uygulama gerektiğinde, `GlobalInterceptor` arayüzünü uygulayın ve Spring Bean olarak yapılandırın:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response Header'ına global eklenir
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Global Ağ Interceptor

`NetworkInterceptor` arayüzünü uygulayın ve Spring Bean olarak yapılandırın.

## Anotasyon Tabanlı Yol Eşleştirme Interceptor

Birçok senaryoda, yalnızca belirli HTTP arayüzlerine bazı özel mantık uygulanması gerekir. Bu durumda yol eşleştirme interceptor kullanarak bu işlev zarif bir şekilde gerçekleştirilebilir.

### BasePathMatchInterceptor'dan Devralarak Interceptor İşleyici Yazma

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response Header'ına path.match eklenir
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Arayüzde @Intercept ile İşaretleme

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// Birden fazla yol eşleştirme interceptor gerekirse, @Intercept eklemeye devam edin
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

 Yukarıdaki `@Intercept` yapılandırması şu anlama gelir: `InterceptorUserService` arayüzü altındaki `/api/user/**` yolundaki (`/api/user/getUser` hariç) istekler intercept edilir, interceptor işleyici `PathMatchInterceptor` kullanılır. Birden fazla interceptor gerektiğinde, arayüz üzerinde birden fazla `@Intercept` anotasyonu işaretlenir.

## Özel Interceptor Anotasyon

Bazen "interceptor anotasyonu" içinde bazı parametreleri dinamik olarak geçmek ve ardından interceptor sırasında bu parametreleri kullanmak gerekir. Bu durumda "özel interceptor anotasyon" kullanılabilir, adımlar aşağıdaki gibidir:

1. Özel anotasyon tanımlama, `@InterceptMark` ile işaretlenmelidir ve anotasyon içinde `include`, `exclude`, `handler` alanları bulunmalıdır
2. `BasePathMatchInterceptor`'dan devralarak interceptor işleyici yazma
3. Arayüzde özel anotasyon kullanma

Aşağıda, "istek başlığına dinamik olarak `accessKeyId`, `accessKeySecret` imza bilgisi ekleme" örneği ile tüm süreç gösterilmiştir.

### Özel @Sign Anotasyon Tanımlama

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

`@Sign` anotasyonunda kullanılan interceptor `SignInterceptor` olarak belirtilmiştir.

### SignInterceptor Uygulama

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

> Not: `accessKeyId` ve `accessKeySecret` alanları için `setter` metodu sağlanmalıdır.

Interceptor'ın `accessKeyId` ve `accessKeySecret` alan değerleri, `@Sign` anotasyonunun `accessKeyId()` ve `accessKeySecret()` değerlerine göre otomatik olarak inject edilir. `@Sign` tarafından belirtilen değer placeholder formatında bir string ise, yapılandırma özellik değeri alınarak inject edilir.

### Arayüzde @Sign Kullanma

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[Önceki: İstek Yeniden Deneme](retry.md) | [Sonraki: Devre Kesici / Geri Dönüş](degrade.md)