# GraalVM Native Image / AOT поддержка
[English](../en/aot.md) | [简体中文](../cn/aot.md) | [繁體中文](../tw/aot.md) | [日本語](../ja/aot.md) | [한국어](../ko/aot.md) | [Español](../es/aot.md) | [Türkçe](../tr/aot.md) | **Русский**

Компонент имеет встроенную поддержку Spring AOT. При компиляции в GraalVM Native Image под Spring Boot 3.x/4.x **работает из коробки, не нужно手описать `reflect-config.json` / `proxy-config.json`**.

На этапе构建ения (`spring-boot:process-aot` или native编译) автоматически注册руются для каждого `@RetrofitClient` интерфейса:

- **JDK динамический proxy**: `Retrofit.create(интерфейс)` и proxy circuit breaker основаны на интерфейсе для создания JDK proxy;
- **Reflection интерфейса**: Сигнатура метода и аннотации параметров должны быть reflection-видимы под native, для解析ения HTTP-запросов Retrofit;
- **Reflection-конструирование классов,引用аемых аннотацией**: `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory` на `@RetrofitClient`, а также `handler` interceptor класс аннотации `@InterceptMark` (включая `@Intercept` / `@Sign`), которые могут быть创建аны через reflection и注入аны属性 на этапе выполнения;
- **Actuator value-объект serialization**: Reflection serialization результата `/actuator/retrofit`.

> Эта功能ность реализуется через `RetrofitAotProcessor` (`BeanFactoryInitializationAotProcessor`), **действует только на этапе AOT构建ения**, обычный JVM启动 и native阶段 выполнения не выполняют任何逻辑, нулевое влияние на функциональность и производительность.
>
> Если ваши кастомные `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` и др. будут сериализованы JSON в сложные бизнесовые实体, native reflection hints сами бизнесовых实体 все еще需要声明овать по стандартным методам Spring (например, `@RegisterReflectionForBinding`) -- это связано с конкретной бизнесовой моделью, не входит в область ответственности компонента.

---

[Предыдущая: Actuator Endpoint](actuator.md) | [Следующая: HTTP-вызовы между микросервисами](microservice.md)