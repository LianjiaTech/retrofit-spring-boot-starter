# Yontem Duzeyi Zaman Asimi Yapilandirmasi
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | **Türkçe** | [Русский](../ru/timeout.md)

Bilesen, `@Timeout` ek aciklamasi ile yontem veya sinif duzeyinde zaman asimi parametrelerinin ayarlanmasini destekler, global zaman asimi yapilandirmasini override eder.

## Oncelik Zinciri

```
Yontem @Timeout -> Sinif @Timeout -> Global Yapilandirma (GlobalTimeoutProperty)
```

- `@Timeout` ozelliginin varsayilan degeri `-1`, "yapilandirilmamis, ust oncelik zincirini devral" anlamina gelir
- `0` degeri "zaman asimi yok" anlamina gelir
- Pozitif deger belirli zaman asimi milisaniye degerini temsil eder
- `-1`, OkHttp zaman asiminin yasadisi deger araligidir (OkHttp yalnizca 0 ve pozitif degerleri kabul eder), "yapilandirilmamis" isareti olarak kullanilir ve gecerli zaman asimi degerleri ile cakismaz

## Dort Zaman Asimi Boyutu

| Ozellik | Anlami | Varsayilan Deger |
|---------|--------|------------------|
| `connectTimeoutMs` | Baglanti zaman asimi (milisaniye) | `-1` (ust duzeyi devralir) |
| `readTimeoutMs` | Okuma zaman asimi (milisaniye) | `-1` (ust duzeyi devralir) |
| `writeTimeoutMs` | Yazma zaman asimi (milisaniye) | `-1` (ust duzeyi devralir) |
| `callTimeoutMs` | Tam cagri zaman asimi (milisaniye) | `-1` (ust duzeyi devralir) |

## Sinif Duzeyi @Timeout

Arayuzde `@Timeout` aciklanarak, arayuzun tum yontemleri icin zaman asimi ayarlanir:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Yontem Duzeyi @Timeout

Belirli bir yontemde `@Timeout` aciklanarak, sinif duzeyi veya global zaman asimi yapilandirmasini override eder:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Sinif duzeyi readTimeoutMs = 5000 degerini devralir
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Yontem duzeyi override: yavas sorgu arayuzu daha uzun zaman asimi kullanir
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Yalnizca Yontem Duzeyi @Timeout (Sinif Duzeyi Ek Aciklamasi Yok)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Global zaman asimi yapilandirmasini kullanir
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Yalnizca bu yontem ozel zaman asimi kullanir
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Gerceklestirme Mekanizmasi

- Sinif duzeyi `@Timeout`: OkHttpClient olusturulurken islenir, calisma zamaninda ek masraf yoktur
- Yontem duzeyi `@Timeout`: `TimeoutCallFactory` ile her yontem icin OkHttpClient clone onceden olusturulur, calisma zamaninda Invocation tag ile arama yapilir, ek aciklamasi olmayan arayuzlerde ek masraf yoktur

---

[Onceki: OkHttpClient Ozellestirme](okhttp-client.md) | [Sonraki: Log Kaydi](logging.md)