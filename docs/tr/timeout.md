# Metot Düzeyinde Zaman Aşımı Yapılandırması
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | **Türkçe** | [Русский](../ru/timeout.md)

Bileşen, `@Timeout` anotasyonu ile metot veya sınıf düzeyinde zaman aşımı parametreleri ayarlanmasını destekler ve global zaman aşımı yapılandırmasını override eder.

## Öncelik Zinciri

```
Metot @Timeout → Sınıf @Timeout → Global Yapılandırma (GlobalTimeoutProperty)
```

- `@Timeout` özellik varsayılan değeri `-1`, "yapılandırılmamış, üst öncelik zincirinden devralma" anlamına gelir
- `0` olarak ayarlanması "zaman aşımı yok" anlamına gelir
- Pozitif bir değer, belirli zaman aşımı milisaniye değerini ifade eder
- `-1`, OkHttp zaman aşımının geçersiz değer alanıdır (OkHttp yalnızca 0 ve pozitif değerleri kabul eder), "yapılandırılmamış" işareti olarak kullanıldığında geçerli zaman aşımı değerleri ile çakışmaz

## Dört Zaman Aşımı Boyutu

| Özellik | Anlam | Varsayılan Değer |
|------|------|--------|
| `connectTimeoutMs` | Bağlantı zaman aşımı (milisaniye) | `-1` (üst düzeyden devralma) |
| `readTimeoutMs` | Okuma zaman aşımı (milisaniye) | `-1` (üst düzeyden devralma) |
| `writeTimeoutMs` | Yazma zaman aşımı (milisaniye) | `-1` (üst düzeyden devralma) |
| `callTimeoutMs` | Tam çağrı zaman aşımı (milisaniye) | `-1` (üst düzeyden devralma) |

## Sınıf Düzeyinde @Timeout

Arayüz üzerinde `@Timeout` bildirimi yaparak, bu arayüzün tüm metodları için zaman aşımı ayarlanır:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Metot Düzeyinde @Timeout

Belirli bir metot üzerinde `@Timeout` bildirimi yaparak, sınıf düzeyi veya global zaman aşımı yapılandırmasını override eder:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Sınıf düzeyi readTimeoutMs = 5000 devralınır
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Metot düzeyinde override: yavaş sorgu arayüzü daha uzun zaman aşımı kullanır
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Yalnızca Metot Düzeyinde @Timeout (Sınıf Düzeyinde Anotasyon Yok)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Global zaman aşımı yapılandırması kullanılır
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Yalnızca bu metot özel zaman aşımı kullanır
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

[Önceki: Özel OkHttpClient](okhttp-client.md) | [Sonraki: Log Yazdırma](logging.md)