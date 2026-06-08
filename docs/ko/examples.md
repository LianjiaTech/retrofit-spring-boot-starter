# 기타 기능 예제
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | **한국어** | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

본 섹션에서는 일반적인 HTTP 요청 시나리오 예제를 소개합니다.

## Form 매개변수

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

## 파일 업로드

### MultipartBody.Part 생성

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

### 업로드 인터페이스 정의

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## 파일 다운로드

### 다운로드 인터페이스 정義

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### 다운로드 사용 예

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
        // 바이너리 스트림
        InputStream is = responseBody.byteStream();

        // 바이너리 스트림 처리는 비즈니스에서 제어합니다. 여기서는 파일 쓰기 예를 보여줍니다
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

## 동적 URL

`@Url` 어노테이션으로 동적 URL을 구현할 수 있습니다. 이 경우 `baseUrl`은任意의 유효 URL을 설정하고, 런타임에서 `@Url` 주소에 기반하여 요청을 발행합니다.

> 주의: `@Url`은 메서드 매개변수의 첫 번째 위치에 배치해야 합니다. 또한, `@GET`, `@POST` 등 어노테이션에서는 엔드포인트 경로를 정의하지 마세요.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE 요청에 요청 본체 추가

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET 요청에 요청 본체 추가

OkHttp 자체는 GET 요청에 요청 본체 추가를 지원하지 않습니다 ([OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154) 참조). 실제로 필요한 경우 소문자 `get`으로 제한을回避할 수 있습니다:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[이전 섹션: 전체 설정 항목 레퍼런스](configuration.md)