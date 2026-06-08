# Adaptación automática de resultados de respuesta HTTP
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | **Español** | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

Este componente adapta automáticamente los resultados de respuesta HTTP al tipo de retorno definido en la interfaz Java. Actualmente se admiten los siguientes tipos de retorno:

- `Call<T>`: no realiza adaptación, devuelve directamente el objeto `Call<T>`
- `String`: adapta el Response Body a `String`
  - Por defecto usa el JSON Converter para convertir los bytes del Response Body a String; si se desea obtener directamente el String convertido del Response Body, se puede especificar `Converter.Factory` como `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- Tipos básicos (`Long`/`Integer`/`Boolean`/`Float`/`Double`): adapta el Response Body al tipo básico correspondiente
- `CompletableFuture<T>`: adapta el Response Body a un objeto `CompletableFuture<T>`
- `Void`: se usa cuando no se necesita el tipo de retorno
- `Response<T>`: adapta la respuesta a un objeto `Retrofit.Response<T>`
- `Mono<T>`: tipo de retorno reactivo de Project Reactor
- `Single<T>`: tipo de retorno reactivo de RxJava (admite RxJava2/RxJava3)
- `Completable`: tipo de retorno reactivo de RxJava, usado para escenarios donde la petición HTTP no tiene cuerpo de respuesta (admite RxJava2/RxJava3)
- Cualquier tipo POJO: adapta el Response Body al objeto POJO correspondiente

## Implementación de adaptación

Retrofit adapta el objeto `Call<T>` al tipo de retorno del método de la interfaz mediante `CallAdapterFactory`. Este componente extiende las siguientes implementaciones de `CallAdapterFactory`:

- **BodyCallAdapterFactory**
  - Ejecuta sincrónicamente la petición HTTP, adapta el contenido del cuerpo de respuesta al tipo de retorno del método
  - Se puede usar con cualquier tipo de retorno de método, tiene la prioridad más baja

- **ResponseCallAdapterFactory**
  - Ejecuta sincrónicamente la petición HTTP, adapta el contenido del cuerpo de respuesta a `Retrofit.Response<T>`
  - Solo se aplica cuando el tipo de retorno del método es `Retrofit.Response<T>`

- **CallAdapterFactory relacionados con programación reactiva**
  - Admite tipos reactivos como `Mono<T>`, `Single<T>`, `Completable`

Heredando `CallAdapter.Factory`, se puede implementar cualquier forma de adaptación de respuesta HTTP al tipo de retorno de la interfaz Java. El componente admite la configuración de fábricas de adaptadores de llamada globales mediante `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # Fábricas de adaptadores globales (las CallAdapterFactory extendidas por el componente ya están integradas, no configure repetidamente)
  global-call-adapter-factories:
    # ...
```

Para cada interfaz Java, también se puede especificar el `CallAdapter.Factory` utilizado por la interfaz actual mediante `@RetrofitClient.callAdapterFactories`.

---

[Volver al índice de funciones](../../README.md) | [Siguiente: Convertidor de datos personalizado](converter.md)