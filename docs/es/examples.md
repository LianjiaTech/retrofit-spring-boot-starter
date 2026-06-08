# Ejemplos de otras funciones
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | **Español** | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

Esta sección recopila ejemplos de escenarios comunes de peticiones HTTP.

## Parámetros Form

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

## Upload de archivos

### Crear MultipartBody.Part

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

### Definición de interfaz de upload

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## Download de archivos

### Definición de interfaz de download

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### Uso de download

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
        // Flujo binario
        InputStream is = responseBody.byteStream();

        // Cómo procesar el flujo binario específicamente lo controla el negocio. Aquí se toma escribir en archivo como ejemplo
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

## URL dinámica

Usando la anotación `@Url` se puede lograr URL dinámica. En este caso, `baseUrl` puede configurarse con cualquier URL válida, y en runtime solo se enviarán peticiones según la dirección de `@Url`.

> Nota: `@Url` debe colocarse en la primera posición del parámetro del método. Además, no es necesario definir la ruta del endpoint en las anotaciones `@GET`, `@POST`, etc.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## Agregar body de petición a peticiones DELETE

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## Agregar body de petición a peticiones GET

OkHttp no admite nativamente agregar body de petición en peticiones GET (ver [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)). Si realmente se necesita, se puede usar la forma minúscula `get` para evitar la restricción:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[Anterior: Referencia completa de configuración](configuration.md)