# Примеры использования
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | **Русский**

В данном разделе собраны примеры для распространенных сценариев HTTP-запросов.

## Form-параметры

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

## Загрузка файлов

### Создание MultipartBody.Part

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

### Определение интерфейса загрузки

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## Скачивание файлов

### Определение интерфейса скачивания

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### Использование скачивания

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
        // Двоичный поток
        InputStream is = responseBody.byteStream();

        // Как处理ать двоичный поток, определяется бизнесовой логикой. Ниже приведен пример записи в файл
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

## Динамический URL

Используйте аннотацию `@Url` для реализации динамического URL. В этом случае `baseUrl` может быть любым допустимым URL, при выполнении запрос будет отправлен только по адресу, указанному в `@Url`.

> Примечание: `@Url` должен быть на первом месте параметра метода. Кроме того, на аннотациях `@GET`, `@POST` и др. не нужно определять путь endpoint.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE-запрос с телом запроса

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET-запрос с телом запроса

OkHttp自身но не поддерживает добавление тела запроса к GET-запросу (см. [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)). Если это действительно нужно, можно использовать小写 `get` для обхода ограничения:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[Предыдущая: Справочник конфигурации](configuration.md)