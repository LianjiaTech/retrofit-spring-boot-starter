# OkHttpClient y Call.Factory personalizados
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | **Español** | [Türkçe](../tr/okhttp-client.md) | [Русский](../ru/okhttp-client.md)

Este componente crea un `OkHttpClient` configurado (incluyendo todos los interceptores, timeouts, pools de conexiones, etc.) para cada interfaz `@RetrofitClient`, y lo utiliza como `Call.Factory` de Retrofit. A continuación se presentan dos formas de personalización:

## OkHttpClient personalizado

Para la configuración relacionada con timeouts, se puede configurar mediante archivos de configuración o la anotación `@Timeout` (ver [Configuración de timeout](timeout.md)). Sin embargo, si se necesita una configuración de OkHttpClient más flexible y compleja, se recomienda implementar un OkHttpClient personalizado.

### Implementar la interfaz SourceOkHttpClientRegistrar

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // Registrar customOkHttpClient, con timeout configurado a 1s
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### Especificar el OkHttpClient usado por la interfaz

Especificar el OkHttpClient que la interfaz actual debe usar mediante `@RetrofitClient.sourceOkHttpClient`:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Call.Factory SPI personalizado

Si se necesita personalizar en el nivel de creación de Call (como callTimeout dinámico, personalización por petición, etc.), se puede implementar mediante el SPI `CallFactoryConfigurer`.

> **¿Por qué se necesita SPI?** El `callTimeout` de OkHttp es el tiempo límite de toda la llamada, no se puede sobrescribir de forma reliable en un interceptor (OkHttp completa la programación del timeout antes de ejecutar la cadena de interceptores). `CallFactoryConfigurer` interviene en el nivel de creación de Call, usando `OkHttpClient.newBuilder()` para derivar un client ligero (compartiendo connectionPool y dispatcher) para lograr la sobrescritura por petición.

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
                        // newBuilder() comparte connectionPool/dispatcher/interceptors, solo callTimeout es diferente
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // Sin sobrescritura → usar @Timeout o el valor predeterminado global
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Aplicar solo a interfaces específicas

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
        // Para otras interfaces, devolver directamente baseClient, equivalente al comportamiento predeterminado
        return baseClient;
    }
}
```

> Cuando no se registra un bean `CallFactoryConfigurer`, el comportamiento del componente permanece completamente inalterado.

---

[Anterior: Convertidor de datos personalizado](converter.md) | [Siguiente: Configuración de timeout por método](timeout.md)