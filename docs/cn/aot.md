# GraalVM Native Image / AOT 支持
[English](../en/aot.md) | **简体中文** | [繁體中文](../tw/aot.md) | [日本語](../ja/aot.md) | [한국어](../ko/aot.md) | [Español](../es/aot.md) | [Türkçe](../tr/aot.md) | [Русский](../ru/aot.md)

组件已内置 Spring AOT 支持，在 Spring Boot 3.x / 4.x 下编译为 GraalVM Native Image 时**开箱即用，无需手写 `reflect-config.json` / `proxy-config.json`**。

构建期（`spring-boot:process-aot` 或 native 编译）会自动为每个 `@RetrofitClient` 接口注册：

- **JDK 动态代理**：`Retrofit.create(接口)` 与熔断降级代理都基于接口生成 JDK 代理；
- **接口反射**：方法签名与参数注解需在 native 下反射可见，供 Retrofit 解析 HTTP 请求；
- **注解引用类的反射构造**：`@RetrofitClient` 上的 `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory`，以及 `@InterceptMark`（含 `@Intercept` / `@Sign`）注解的 `handler` 拦截器类，运行期可能通过反射创建并注入属性；
- **Actuator 值对象序列化**：`/actuator/retrofit` 返回结果的反射序列化。

> 该能力由 `RetrofitAotProcessor`（`BeanFactoryInitializationAotProcessor`）实现，**仅在 AOT 构建期生效**，普通 JVM 启动与 native 运行期不执行任何逻辑，对功能与性能零影响。
>
> 若你自定义的 `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` 等会被 JSON 序列化为复杂业务实体，业务实体本身的 native 反射 hints 仍需按 Spring 标准方式（如 `@RegisterReflectionForBinding`）声明——这与具体业务模型相关，不在组件职责范围内。

---

[上一节：Actuator Endpoint](actuator.md) | [下一节：微服务之间的 HTTP 调用](microservice.md)