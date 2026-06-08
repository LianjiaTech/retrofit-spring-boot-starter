# HTTP Response Adaptation
**English** | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

This component automatically adapts HTTP response results to the return type defined in the Java interface. The following return types are currently supported:

- `Call<T>`: No adaptation is performed; the `Call<T>` object is returned directly
- `String`: Adapts the Response Body to a `String` return value
  - By default, the JSON Converter converts the Response Body bytes to a String; if you want to directly get the String from the Response Body, you can specify `Converter.Factory` as `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- Primitive types (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Adapts the Response Body to the corresponding primitive type
- `CompletableFuture<T>`: Adapts the Response Body to a `CompletableFuture<T>` object
- `Void`: Used when the return type is not of interest
- `Response<T>`: Adapts the response to a `Retrofit.Response<T>` object
- `Mono<T>`: Project Reactor reactive return type
- `Single<T>`: RxJava reactive return type (supports RxJava2/RxJava3)
- `Completable`: RxJava reactive return type, used for HTTP requests with no response body (supports RxJava2/RxJava3)
- Any POJO type: Adapts the Response Body to the corresponding POJO object

## Adaptation Implementation

Retrofit uses `CallAdapterFactory` at the underlying level to adapt `Call<T>` objects to the interface method's return type. This component extends the following `CallAdapterFactory` implementations:

- **BodyCallAdapterFactory**
  - Synchronously executes HTTP requests and adapts the response body content to the method's return type
  - Can be used for any method return type; has the lowest priority

- **ResponseCallAdapterFactory**
  - Synchronously executes HTTP requests and adapts the response body content to a `Retrofit.Response<T>` return value
  - Only effective when the method return type is `Retrofit.Response<T>`

- **Reactive programming CallAdapterFactory**
  - Supports reactive types such as `Mono<T>`, `Single<T>`, and `Completable`

By extending `CallAdapter.Factory`, you can implement any form of HTTP response to Java interface return type adaptation. The component supports configuring global call adapter factories via `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # Global adapter factories (the extended CallAdapterFactory is already built-in, do not configure duplicates)
  global-call-adapter-factories:
    # ...
```

For each Java interface, you can also specify the `CallAdapter.Factory` to use via `@RetrofitClient.callAdapterFactories`.

---

[Back to Feature Index](../../README.md) | [Next: Custom Data Converter](converter.md)