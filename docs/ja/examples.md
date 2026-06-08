# その他機能例
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | **日本語** | [한국어](../ko/examples.md) | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

本節ではよくある HTTP リクエスト場面の例を紹介します。

## Form パラメーター

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

## ファイルアップロード

### MultipartBody.Part の作成

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

### アップロードインターフェース定義

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## ファイルダウンロード

### ダウンロードインターフェース定義

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### ダウンロードの使用

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
        // バイナリストリーム
        InputStream is = responseBody.byteStream();

        // バイナリストリームの具体的な処理方法はビジネス側で制御。ここではファイル書き込みの例
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

## 動的 URL

`@Url` アノテーションで動的 URL を実現できます。この場合 `baseUrl` は任意の合法 URL を設定即可、実行時は `@Url` のアドレスのみに基づいてリクエストを発行します。

> 注意：`@Url` はメソッドパラメーターの最初の位置に配置する必要があります。また、`@GET`、`@POST` 等のアノテーションにエンドポイントパスを定義する必要はありません。

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE リクエストにリクエストボディを追加

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET リクエストにリクエストボディを追加

OkHttp 自体は GET リクエストにリクエストボディを追加することをサポートしていません（[OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)を参照）。本当に必要な場合は、小文字の `get` を使用して制限を回避できます：

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[前へ：全設定項目参考](configuration.md)