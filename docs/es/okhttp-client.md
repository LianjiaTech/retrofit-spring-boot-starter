# OkHttpClient personalizado y Call.Factory SPI
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | **Español** | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

Este componente crea un `OkHttpClient` configurado para cada interfaz `@RetrofitClient` (con todos los interceptores, timeouts, pools de conexiones, etc.) y lo usa como `Call.Factory` de Retrofit. A continuacion se presentan dos formas de personalizacion:

## OkHttpClient personalizado

Para la configuracion de timeouts, se puede usar archivos de configuracion o la anotacion `@Timeout` (ver [Configuracion de timeout](timeout.md)). Sin embargo, si se necesita una configuracion de OkHttpClient mas flexible y compleja, se recomienda implementar un OkHttpClient personalizado.

### Implementar la interfaz SourceOkHttpClientRegistrar

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // Registrar customOkHttpClient, timeout configurado a 1s
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### Especificar el OkHttpClient utilizado por la interfaz

Usar `@RetrofitClient.sourceOkHttpClient` para especificar el OkHttpClient que la interfaz actual debe usar:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Call.Factory SPI personalizado

Si se necesita personalizar a nivel de creacion de Call (como callTimeout dinamico, personalizacion a nivel de solicitud, etc.), se puede implementar mediante `CallFactoryConfigurer` SPI.

> **Por que se necesita SPI?** El `callTimeout` de OkHttp es el tiempo limite de toda la llamada, y no puede ser sobrescrito de forma confiable en un interceptor (OkHttp completa la programacion de timeout antes de ejecutar la cadena de interceptores). `CallFactoryConfigurer` se involucra a nivel de creacion de Call, usando `OkHttpClient.newBuilder()` para derivar un client ligero (que comparte connectionPool y dispatcher) para lograr la cobertura por solicitud.

### Implementar la interfaz CallFactoryConfigurer

```java
@Component
public class DynamicCallTimeoutConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        return new Call.Factory() {
            @Override
            public Call newCall(Request request) {
                Invocation invocation = request.tag(Invocation.class);
                if (invocation != null) {
                    MyCallTimeout ann = invocation.method().getAnnotation(MyCallTimeout.class);
                    if (ann != null) {
                        // newBuilder() comparte connectionPool/dispatcher/interceptors, solo callTimeout diferente
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // Sin cobertura -> usar @Timeout o valores globales por defecto
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Aplicar solo a interfaces especificas

```java
@Component
public class SelectiveCallFactoryConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        if (retrofitInterface == SlowApiService.class) {
            return baseClient.newBuilder()
                    .callTimeout(30_000, TimeUnit.MILLISECONDS)
                    .build();
        }
        // Otras interfaces retornan baseClient directamente, equivalente al comportamiento por defecto
        return baseClient;
    }
}
```

> Cuando no se registra un Bean de `CallFactoryConfigurer`, el comportamiento del componente no cambia en absoluto. Cuando `CallFactoryConfigurer` retorna algo que no es `OkHttpClient`, `@Timeout` a nivel de metodo no surte efecto -- el usuario debe manejar el timeout en su implementacion personalizada.

---

[Anterior: Convertidor de datos personalizado](converter.md) | [Siguiente: Configuracion de timeout a nivel de metodo](timeout.md)