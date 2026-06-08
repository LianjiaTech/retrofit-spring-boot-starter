# Interceptor
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | **Español** | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

Este componente proporciona cuatro mecanismos de interceptor para satisfacer las necesidades de interceptación de peticiones HTTP en diferentes escenarios.

## Interceptor de aplicación global

Cuando se necesita ejecutar interceptación uniforme en todas las peticiones HTTP del sistema, implementar la interfaz `GlobalInterceptor` y configurarla como bean de Spring:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Agregar "global" al Header de response
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Interceptor de red global

Implementar la interfaz `NetworkInterceptor` y configurarla como bean de Spring.

## Interceptor de coincidencia de rutas por anotación

En muchos escenarios, se necesita aplicar lógica especial solo a ciertas interfaces HTTP. En este caso, se puede usar el interceptor de coincidencia de rutas para implementar esta funcionalidad de forma elegante.

### Heredar BasePathMatchInterceptor para escribir el procesador de interceptación

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Agregar "path.match" al Header de response
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Usar @Intercept para marcar en la interfaz

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// Si se necesitan múltiples interceptores de coincidencia de rutas, continuar agregando @Intercept
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

La configuración `@Intercept` anterior significa: interceptar las peticiones bajo la ruta `/api/user/**` (excluyendo `/api/user/getUser`) de la interfaz `InterceptorUserService`, usando `PathMatchInterceptor` como procesador de interceptación. Si se necesitan múltiples interceptores, se pueden marcar múltiples anotaciones `@Intercept` en la interfaz.

## Anotación de interceptor personalizada

A veces se necesita pasar dinámicamente algunos parámetros en la "anotación de interceptación" y usarlos durante la interceptación. En este caso, se puede usar una "anotación de interceptor personalizada", los pasos son los siguientes:

1. Crear una anotación personalizada, debe estar marcada con `@InterceptMark`, y la anotación debe incluir los campos `include`, `exclude`, `handler`
2. Heredar `BasePathMatchInterceptor` para escribir el procesador de interceptación
3. Usar la anotación personalizada en la interfaz

A continuación se demuestra el flujo completo usando el ejemplo de "agregar dinámicamente información de firma `accessKeyId`, `accessKeySecret` en los headers de petición".

### Anotación @Sign personalizada

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

En la anotación `@Sign` se ha especificado que el interceptor usado es `SignInterceptor`.

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

> Nota: los campos `accessKeyId` y `accessKeySecret` deben proporcionar métodos `setter`.

Los valores de los campos `accessKeyId` y `accessKeySecret` del interceptor se inyectarán automáticamente según los valores de `accessKeyId()` y `accessKeySecret()` de la anotación `@Sign`. Si `@Sign` especifica una cadena en formato de placeholder, se tomará el valor de la propiedad de configuración para la inyección.

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

[Anterior: Reintento de petición](retry.md) | [Siguiente: Circuit breaker/degradación](degrade.md)