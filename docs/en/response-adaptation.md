# HTTP Response Adaptation
**English** | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

This component automatically adapts HTTP responses to the return types defined in Java interfaces. The following return types are currently supported:

- `Call<T>`: Returns the `Call<T>` object directly without adaptation
- `String`: Adapts the Response Body to a `String`
  - By default, JSON Converter is used to convert Response Body bytes to String; if you want to directly get the String from the Response Body, you can specify `Converter.Factory` as `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- Primitive types (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Adapts the Response Body to the corresponding primitive type
- `CompletableFuture<T>`: Adapts the Response Body to a `CompletableFuture<T>` object
- `Void`: Use when the return type is irrelevant
- `Response<T>`: Adapts the response to a `Retrofit.Response<T>` object
- `Mono<T>`: Project Reactor reactive return type
- `Single<T>`: RxJava reactive return type (supports RxJava2/RxJava3)
- `Completable`: RxJava reactive return type for HTTP requests with no response body (supports RxJava2/RxJava3)
- Any POJO type: Adapts the Response Body to the corresponding POJO object

## Adaptation Implementation

Retrofit uses `CallAdapterFactory` at the underlying level to adapt `Call<T>` objects to the return value types of interface methods. This component extends the following `CallAdapterFactory` implementations:

- **BodyCallAdapterFactory**
  - Executes HTTP requests synchronously and adapts the response body content to the method's return value type
  - Can be used with any method return value type, with the lowest priority

- **ResponseCallAdapterFactory**
  - Executes HTTP requests synchronously and adapts the response body content to `Retrofit.Response<T>`
  - Only effective when the method return value type is `Retrofit.Response<T>`

- **Reactive Programming CallAdapterFactory**
  - Supports reactive types such as `Mono<T>`, `Single<T>`, `Completable`, etc.

By extending `CallAdapter.Factory`, you can implement any form of HTTP response to Java interface return type adaptation. The component supports configuring global call adapter factories via `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # Global adapter factories (component-extended CallAdapterFactory is pre-included, do not reconfigure)
  global-call-adapter-factories:
    # ...
```

For each Java interface, you can also specify the `CallAdapter.Factory` to use via `@RetrofitClient.callAdapterFactories`.

---

[Back to Feature Index](../../README.md) | [Next: Custom Data Converters](converter.md)