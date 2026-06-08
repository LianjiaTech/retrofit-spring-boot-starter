# Additional Examples
**English** | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

This section covers common HTTP request scenario examples.

## Form Parameters

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

## File Upload

### Create MultipartBody.Part

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

### Upload Interface Definition

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## File Download

### Download Interface Definition

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### Download Usage

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
        // Binary stream
        InputStream is = responseBody.byteStream();

        // How to process the binary stream is controlled by the business. Here we write to a file as an example
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

## Dynamic URL

Use the `@Url` annotation to achieve dynamic URLs. In this case, configure any valid URL for `baseUrl` -- at runtime, requests will only be sent based on the `@Url` address.

> Note: `@Url` must be placed in the first position of method parameters. Also, endpoint paths do not need to be defined on `@GET`, `@POST`, and other annotations.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE Request with Request Body

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET Request with Request Body

OkHttp itself does not support adding a request body to GET requests (see [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)). If you really need this, you can use lowercase `get` to bypass the restriction:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[Previous: Configuration Properties Reference](configuration.md)