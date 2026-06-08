# Diger Ozellik Ornekleri
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | [Español](../es/examples.md) | **Türkçe** | [Русский](../ru/examples.md)

Bu bolum yaygin HTTP istek senaryo orneklerini sunar.

## Form Parametreleri

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

## Dosya Yukleme

### MultipartBody.Part Olusturma

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

### Yukleme Arayuz Tanimlama

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## Dosya Indirme

### Indirme Arayuz Tanimlama

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### Indirme Kullanimi

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
        // Ikili akis
        InputStream is = responseBody.byteStream();

        // Ikili akisin nasil islenmesi is tarafinda kontrol edilir. Dosya yazma ornegi
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

`@Url` ek aciklamasi ile dinamik URL gerceklestirilebilir. Bu durumda `baseUrl` herhangi bir gecerli URL yapilandirilabilir, calisma zamaninda yalnizca `@Url` adresine gore istek baslatilir.

> Not: `@Url` yontem parametresinin ilk konumunda olmalidir. Ayrıca, `@GET`, `@POST` vb. ek aciklamalarinda endpoint yolu tanimlanmasi gerekmez.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE Istegine Istek Govdesi Ekleme

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET Istegine Istek Govdesi Ekleme

OkHttp'in kendisi GET isteginde istek govdesi ekleme desteklemez (bkz. [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)). Gercekten gerekirse, kucuk harf `get` ile siniri asilabilir:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[Onceki: Yapilandirma Ozellikleri Basvurusu](configuration.md)