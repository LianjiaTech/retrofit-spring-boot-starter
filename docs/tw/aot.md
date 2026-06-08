# GraalVM Native Image / AOT 支持
[English](../en/aot.md) | [简体中文](../cn/aot.md) | **繁體中文** | [日本語](../ja/aot.md) | [한국어](../ko/aot.md) | [Español](../es/aot.md) | [Türkçe](../tr/aot.md) | [Русский](../ru/aot.md)

元件已內建 Spring AOT 支持，在 Spring Boot 3.x / 4.x 下編譯為 GraalVM Native Image 时**開箱即用，無需手寫 `reflect-config.json` / `proxy-config.json`**。

構建期（`spring-boot:process-aot` 或 native 編譯）会自動為每個 `@RetrofitClient` 介面注册：

- **JDK 動態代理**：`Retrofit.create(介面)` 与熔断降级代理都基於介面生成 JDK 代理；
- **介面反射**：方法签名与參數注解需在 native 下反射可見，供 Retrofit 解析 HTTP 請求；
- **注解引用類的反射構造**：`@RetrofitClient` 上的 `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory`，以及 `@InterceptMark`（含 `@Intercept` / `@Sign`）注解的 `handler` 拦截器類，執行期可能透過反射建立並注入屬性；
- **Actuator 值物件序列化**：`/actuator/retrofit` 回傳结果的反射序列化。

> 该能力由 `RetrofitAotProcessor`（`BeanFactoryInitializationAotProcessor`）實作，**僅在 AOT 構建期生效**，普通 JVM 啟動与 native 執行期不執行任何邏輯，對功能与效能零影響。
>
> 若你自定义的 `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` 等会被 JSON 序列化為複雜業務實體，業務實體本身的 native 反射 hints 仍需按 Spring 標準方式（如 `@RegisterReflectionForBinding`）宣告——這与具體業務模型相關，不在元件職責範圍内。

---

[上一節：Actuator Endpoint](actuator.md) | [下一節：微服务之间的 HTTP 调用](microservice.md)