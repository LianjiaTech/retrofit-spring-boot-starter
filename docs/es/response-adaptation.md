# Adaptacion automatica de resultados de respuesta HTTP
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | **Español** | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

Este componente adapta automaticamente los resultados de respuesta HTTP al tipo de retorno definido en la interfaz Java. Actualmente soporta los siguientes tipos de retorno:

- `Call<T>`: no realiza adaptacion, retorna directamente el objeto `Call<T>`
- `String`: adapta el Response Body a `String`
  - Por defecto usa el JSON Converter para convertir los bytes del Response Body a String; si se desea obtener directamente el String convertido del Response Body, se puede especificar `Converter.Factory` como `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- Tipos basicos (`Long`/`Integer`/`Boolean`/`Float`/`Double`): adapta el Response Body al tipo basico correspondiente
- `CompletableFuture<T>`: adapta el Response Body a un objeto `CompletableFuture<T>`
- `Void`: se usa cuando no se requiere un tipo de retorno
- `Response<T>`: adapta la respuesta a un objeto `Retrofit.Response<T>`
- `Mono<T>`: tipo de retorno reactivo de Project Reactor
- `Single<T>`: tipo de retorno reactivo de RxJava (soporta RxJava2/RxJava3)
- `Completable`: tipo de retorno reactivo de RxJava, usado para solicitudes HTTP sin cuerpo de respuesta (soporta RxJava2/RxJava3)
- Cualquier tipo POJO: adapta el Response Body al objeto POJO correspondiente

## Implementacion de la adaptacion

Retrofit adapta los objetos `Call<T>` al tipo de retorno del metodo de la interfaz mediante `CallAdapterFactory`. Este componente extiende las siguientes implementaciones de `CallAdapterFactory`:

- **BodyCallAdapterFactory**
  - Ejecuta la solicitud HTTP de forma sincronica, adaptando el contenido del cuerpo de respuesta al tipo de retorno del metodo
  - Puede usarse con cualquier tipo de retorno, tiene la prioridad mas baja

- **ResponseCallAdapterFactory**
  - Ejecuta la solicitud HTTP de forma sincronica, adaptando el contenido del cuerpo de respuesta a `Retrofit.Response<T>`
  - Solo se activa cuando el tipo de retorno del metodo es `Retrofit.Response<T>`

- **CallAdapterFactory relacionados con programacion reactiva**
  - Soporta tipos reactivos como `Mono<T>`, `Single<T>`, `Completable`, etc.

Heredando `CallAdapter.Factory`, se puede implementar cualquier forma de adaptacion de respuesta HTTP al tipo de retorno de la interfaz Java. El componente soporta la configuracion de fabricas de adaptadores de llamadas globales mediante `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # Fabricas de adaptadores globales (las CallAdapterFactory extendidas por el componente ya estan integradas, no las configure repetidamente)
  global-call-adapter-factories:
    # ...
```

Para cada interfaz Java, se puede especificar el `CallAdapter.Factory` utilizado por la interfaz actual mediante `@RetrofitClient.callAdapterFactories`.

---

[Índice de características](../README.md) | [Siguiente: Convertidor de datos](converter.md)