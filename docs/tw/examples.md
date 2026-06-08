# 其他功能範例
[English](../en/examples.md) | [简体中文](../cn/examples.md) | **繁體中文** | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

本節收錄常見的 HTTP 請求場景範例。

## Form 參數

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

## 檔案上傳

### 建立 MultipartBody.Part

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

### 上傳介面定義

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## 檔案下載

### 下載介面定義

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### 下載使用

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
        // 二進制流
        InputStream is = responseBody.byteStream();

        // 具體如何處理二進制流，由業務自行控制。這裡以寫入檔案為例
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

## 動態 URL

使用 `@Url` 註解可實作動態 URL。此時 `baseUrl` 設定任意合法 URL 即可，執行時只會根據 `@Url` 地址發起請求。

> 注意：`@Url` 必須放在方法參數的第一個位置。另外，`@GET`、`@POST` 等註解上不需要定義端點路徑。

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE 請求添加請求體

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET 請求添加請求體

OkHttp 自身不支援 GET 請求添加請求體（參見 [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)）。如果確實需要，可以使用小寫 `get` 繞過限制：

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[上一節：全量設定項參考](configuration.md)