# 其他功能示例
[English](../en/examples.md) | **简体中文** | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

本节收录常见的 HTTP 请求场景示例。

## Form 参数

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

## 文件上传

### 创建 MultipartBody.Part

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

### 上传接口定义

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## 文件下载

### 下载接口定义

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### 下载使用

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
        // 二进制流
        InputStream is = responseBody.byteStream();

        // 具体如何处理二进制流，由业务自行控制。这里以写入文件为例
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

## 动态 URL

使用 `@Url` 注解可实现动态 URL。此时 `baseUrl` 配置任意合法 URL 即可，运行时只会根据 `@Url` 地址发起请求。

> 注意：`@Url` 必须放在方法参数的第一个位置。另外，`@GET`、`@POST` 等注解上不需要定义端点路径。

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE 请求添加请求体

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET 请求添加请求体

OkHttp 自身不支持 GET 请求添加请求体（参见 [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)）。如果确实需要，可以使用小写 `get` 绕过限制：

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[上一节：全量配置项参考](configuration.md)