# Otros ejemplos de funcionalidad
[English](../en/examples.md) | [简体中文](../cn/examples.md) | [繁體中文](../tw/examples.md) | [日本語](../ja/examples.md) | [한국어](../ko/examples.md) | **Español** | [Türkçe](../tr/examples.md) | [Русский](../ru/examples.md)

Esta seccion recopila ejemplos de escenarios comunes de solicitudes HTTP.

## Parametros Form

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

## Subida de archivos

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

### Definicion de interfaz de subida

```java
@POST("upload")
@Multipart
Void upload(@Part MultipartBody.Part file);
```

## Descarga de archivos

### Definicion de interfaz de descarga

```java
@RetrofitClient(baseUrl = "https://img.ljcdn.com/hc-picture/")
public interface DownloadApi {

    @GET("{fileKey}")
    Response<ResponseBody> download(@Path("fileKey") String fileKey);
}
```

### Uso de descarga

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
        // Stream binario
        InputStream is = responseBody.byteStream();

        // Como procesar el stream binario especificamente, es controlado por el negocio. Aqui se usa escribir en archivo como ejemplo
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

## URL dinamica

Usar la anotacion `@Url` para implementar URL dinamica. En este caso, configurar `baseUrl` con cualquier URL valida; en tiempo de ejecucion solo se enviará la solicitud basada en la direccion `@Url`.

> Nota: `@Url` debe estar en la primera posicion de los parametros del metodo. Ademas, no se necesita definir el path del endpoint en las anotaciones `@GET`, `@POST`, etc.

```java
@GET
Map<String, Object> test3(@Url String url, @Query("name") String name);
```

## Agregar cuerpo de solicitud a DELETE

```java
@HTTP(method = "DELETE", path = "/user/delete", hasBody = true)
```

## Agregar cuerpo de solicitud a GET

OkHttp no soporta por si mismo agregar cuerpo de solicitud a solicitudes GET (ver [OkHttp Issue #3154](https://github.com/square/okhttp/issues/3154)). Si realmente se necesita, se puede usar `get` en minusculas para evitar la restriccion:

```java
@HTTP(method = "get", path = "/user/get", hasBody = true)
```

---

[Anterior: Referencia completa de configuracion](configuration.md) | [Siguiente: Preguntas frecuentes](faq.md)