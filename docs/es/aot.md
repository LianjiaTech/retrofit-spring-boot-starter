# Soporte GraalVM Native Image / AOT
[English](../en/aot.md) | [简体中文](../cn/aot.md) | [繁體中文](../tw/aot.md) | [日本語](../ja/aot.md) | [한국어](../ko/aot.md) | **Español** | [Türkçe](../tr/aot.md) | [Русский](../ru/aot.md)

El componente tiene soporte Spring AOT integrado. En Spring Boot 3.x / 4.x, la compilacion como GraalVM Native Image es **funcional directamente, sin necesidad de escribir manualmente `reflect-config.json` / `proxy-config.json`**.

Durante el periodo de construccion (`spring-boot:process-aot` o compilacion native), se registran automaticamente para cada interfaz `@RetrofitClient`:

- **Proxy dinamico JDK**: `Retrofit.create(interfaz)` y el proxy de circuit breaker / degradacion se basan en la interfaz para generar proxy JDK;
- **Reflexion de interfaz**: Las firmas de metodos y anotaciones de parametros deben ser visibles por reflexion en native, para que Retrofit解析e las solicitudes HTTP;
- **Construccion por reflexion de clases referenciadas en anotaciones**: `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory` en `@RetrofitClient`, y la clase `handler` de interceptor en la anotacion `@InterceptMark` (incluyendo `@Intercept` / `@Sign`), que pueden ser creadas por reflexion e inyectadas con propiedades en tiempo de ejecucion;
- **Serializacion de objetos de valor de Actuator**: Reflexion de serializacion de resultados retornados por `/actuator/retrofit`.

> Esta capacidad es implementada por `RetrofitAotProcessor` (`BeanFactoryInitializationAotProcessor`), **solo es efectiva durante el periodo de construccion AOT**, en inicio JVM normal y tiempo de ejecucion native no se ejecuta ninguna logica, con impacto zero en funcionalidad y rendimiento.
>
> Si tus `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` personalizados son serializados por JSON a entidades de negocio complejas, los hints de reflexion native de las entidades de negocio mismas deben ser declarados de la forma standard de Spring (como `@RegisterReflectionForBinding`) -- esto esta relacionado con el modelo de negocio especifico y no esta dentro del alcance de responsabilidad del componente.

---

[Anterior: Actuator Endpoint](actuator.md) | [Siguiente: Invocacion HTTP entre microservicios](microservice.md)