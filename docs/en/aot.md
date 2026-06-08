# GraalVM Native Image / AOT Support
**English** | [简体中文](../cn/aot.md) | [繁體中文](../tw/aot.md) | [日本語](../ja/aot.md) | [한국어](../ko/aot.md) | [Español](../es/aot.md) | [Türkçe](../tr/aot.md) | [Русский](../ru/aot.md)

The starter includes built-in Spring AOT support. Compiling to a GraalVM Native Image under Spring Boot 3.x / 4.x works **out of the box -- no hand-written `reflect-config.json` / `proxy-config.json` required**.

At build time (`spring-boot:process-aot` or native compilation), it automatically registers for each `@RetrofitClient` interface:

- **JDK dynamic proxies**: Both `Retrofit.create(interface)` and circuit breaking fallback proxies generate JDK proxies from the interface;
- **Interface reflection**: Method signatures and parameter annotations must be reflectively visible under native image for Retrofit to parse HTTP requests;
- **Reflective construction of annotation-referenced types**: `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory` on `@RetrofitClient`, plus the `handler` interceptor classes referenced by `@InterceptMark` annotations (including `@Intercept` / `@Sign`), which may be created reflectively and have properties injected at runtime;
- **Actuator value-object serialization**: Reflective serialization of the `/actuator/retrofit` response results.

> This capability is implemented by `RetrofitAotProcessor` (a `BeanFactoryInitializationAotProcessor`) and **only runs at AOT build time**. It executes no logic during normal JVM startup or at native runtime, so it has zero impact on functionality and performance.
>
> If your custom `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` etc. serializes complex business entities via JSON, the native reflection hints for those business entities still need to be declared the standard Spring way (e.g. `@RegisterReflectionForBinding`) -- this is tied to your specific domain model and is out of scope for the starter.

---

[Previous: Actuator Endpoint](actuator.md) | [Next: HTTP Calls Between Microservices](microservice.md)