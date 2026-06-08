# 다른 기능 예시
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | **한국어** | [Español](../es/examples.md) | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

이 섹션은常见的 HTTP 요청 시나리오 예시를收录합니다.

## Form 파라미터

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

### 다운로드 인터페이스 정의

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### 다운로드 사용

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

        // 바이너리 스트림의 처리 방식은 비즈니스에서自行으로 제어합니다. 여기서는 파일에 작성하는 예시입니다
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

`@Url` 어노테이션으로 동적 URL을 구현할 수 있습니다. 이때 `baseUrl`에任意合法 URL을 설정하면 되며, 실행 시에는 `@Url` 주소로만 요청을发起합니다.

> 참고: `@Url`은 메서드 파라미터의 첫 번째 위치에 배치必须합니다. 또한, `@GET`, `@POST` 등 어노테이션에 엔드포인트 경로를 정의할 필요가 없습니다.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## DELETE 요청에 요청 본문 추가

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## GET 요청에 요청 본문 추가

OkHttp 자체는 GET 요청에 요청 본문 추가를 지원하지 않습니다([OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154) 참조). 필요한 경우, 소문자 `get`을 사용하여 제한을 우회할 수 있습니다:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[이전: 전체 설정 항목 참조](configuration.md)