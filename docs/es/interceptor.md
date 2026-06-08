# Interceptores
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | **Español** | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

Este componente proporciona cuatro mecanismos de interceptores para satisfacer las necesidades de interception de solicitudes HTTP en diferentes escenarios.

## Interceptor de aplicacion global

Cuando se necesita realizar un procesamiento de interception unificado para todas las solicitudes HTTP del sistema, implementar la interfaz `GlobalInterceptor` y configurarla como un Spring Bean:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Agregar header "global" al response
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Interceptor de red global

Implementar la interfaz `NetworkInterceptor` y configurarla como un Spring Bean.

## Interceptor declarativo con coincidencia de ruta

En muchos escenarios, se necesita aplicar logica especial solo a ciertas interfaces HTTP. Se puede usar el interceptor con coincidencia de ruta para implementar elegantemente esta funcionalidad.

### Heredar BasePathMatchInterceptor para escribir el procesador de interception

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Agregar header "path.match" al response
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Usar @Intercept en la interfaz

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// Si se necesita usar multiples interceptores con coincidencia de ruta, continuar agregando @Intercept
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

La configuracion `@Intercept` anterior significa: interceptar las solicitudes bajo la ruta `/api/user/**` (excluyendo `/api/user/getUser`) de la interfaz `InterceptorUserService`, usando `PathMatchInterceptor` como procesador. Si se necesitan multiples interceptores, se pueden anadir multiples anotaciones `@Intercept` en la interfaz.

## Anotacion de interceptor personalizada

A veces se necesita pasar dinamicamente algunos parametros en la "anotacion de interception" y usar estos parametros durante la interception. Se puede usar una "anotacion de interceptor personalizada", los pasos son:

1. Crear una anotacion personalizada, debe estar marcada con `@InterceptMark`, y la anotacion debe incluir los campos `include`, `exclude`, `handler`
2. Heredar `BasePathMatchInterceptor` para escribir el procesador de interception
3. Usar la anotacion personalizada en la interfaz

A continuacion se demuestra el proceso completo usando el ejemplo de "agregar dinamicamente informacion de firma `accessKeyId` y `accessKeySecret` en los headers de solicitud".

### Anotacion @Sign personalizada

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {

    String accessKeyId();

    String accessKeySecret();

    String[] include() default {"/**"};

    String[] exclude() default {};

    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

En la anotacion `@Sign` se especifica que el interceptor usado es `SignInterceptor`.

### Implementar SignInterceptor

```java
@Component
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
        Response response = chain.proceed(newReq);
        return response.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
    }
}
```

> Nota: los campos `accessKeyId` y `accessKeySecret` deben proporcionar metodos `setter`.

Los valores de los campos `accessKeyId` y `accessKeySecret` del interceptor se inyectan automaticamente segun los valores de `accessKeyId()` y `accessKeySecret()` de la anotacion `@Sign`. Si `@Sign` especifica una cadena en formato de placeholder, se tomará el valor de la propiedad de configuracion para la inyeccion.

### Usar @Sign en la interfaz

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[Anterior: Reintento de solicitudes](retry.md) | [Siguiente: Circuit Breaker / Degradacion](degrade.md)