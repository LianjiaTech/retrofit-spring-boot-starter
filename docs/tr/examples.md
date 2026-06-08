# Diğer İşlev Örnekleri
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | [Español](../es/examples.md) | **Türkçe** | [Русский](../ru/examples.md)

Bu bölümde yaygın HTTP istek senaryo örnekleri yer almaktadır.

## Form Parametre

```java
@FormUrlEncoded
@POST("token/verify")
Object tokenVerify(@Field("source") String source,
                   @Field("signature") String signature,
                   @Field("token") String token);

@FormUrlEncoded
@POST("message")
CompletableFuture<Object> sendMessage(@FieldMap Map<String, Object> param);
```

## Dosya Yükleme

### MultipartBody.Part Oluşturma

```java
public ResponseEntity importTerminology(MultipartFile file) {
    String fileName = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "utf-8");
    okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
            MediaType.parse("multipart/form-data"), file.getBytes());
    MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);
    apiService.upload(part);
    return ok().build();
}
```

### Yükleme Arayüz Tanımı

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## Dosya İndirme

### İndirme Arayüz Tanımı

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### İndirme Kullanımı

```java
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class DownloadTest {
    @Autowired
    DownloadApi downloadApi;

    @Test
    public void download() throws Exception {
        String fileKey = "6302d742-ebc8-4649-95cf-62ccf57a1add";
        Response<ResponseBody> response = downloadApi.download(fileKey);
        ResponseBody responseBody = response.body();
        // Binary akış
        InputStream is = responseBody.byteStream();

        // Binary akışın nasıl işleneceği iş tarafından kontrol edilir. Burada dosya yazma örneği verilmiştir
        File tempDirectory = new File("temp");
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
        }
        File file = new File(tempDirectory, UUID.randomUUID().toString());
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        byte[] b = new byte[1024];
        int length;
        while ((length = is.read(b)) > 0) {
            fos.write(b, 0, length);
        }
        is.close();
        fos.close();
    }
}
```

## Dinamik URL

`@Url` anotasyonu ile dinamik URL gerçekleştirilebilir. Bu durumda `baseUrl` herhangi bir geçerli URL olarak yapılandırılabilir, çalışma zamanında yalnızca `@Url` adresine göre istek gönderilir.

> Not: `@Url` metot parametresinin ilk konumunda olmalıdır. Ayrıca, `@GET`, `@POST` gibi anotasyonlarda endpoint yol tanımı yapılması gerekmez.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE İsteğine İstek Gövdesi Ekleme

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET İsteğine İstek Gövdesi Ekleme

OkHttp, GET isteğine istek gövdesi eklemeyi kendi olarak desteklemez ([OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154) bkz.). Gerekirse, küçük harf `get` kullanarak sınırı aşabilirsiniz:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[Önceki: Tam Yapılandırma Öğeleri Referansı](configuration.md)