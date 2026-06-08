# HTTP 响应结果自动适配
[English](../en/response-adaptation.md) | **简体中文** | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

本组件会将 HTTP 响应结果自动适配成 Java 接口定义的返回类型，目前支持以下几种返回类型：

- `Call<T>`：不执行适配处理，直接返回 `Call<T>` 对象
- `String`：将 Response Body 适配成 `String` 返回
  - 默认使用 JSON Converter 将 Response Body 的 bytes 转成 String；如果想直接得到 Response Body 转成的 String，可以指定 `Converter.Factory` 为 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- 基础类型（`Long`/`Integer`/`Boolean`/`Float`/`Double`）：将 Response Body 适配成对应基础类型
- `CompletableFuture<T>`：将 Response Body 适配成 `CompletableFuture<T>` 对象返回
- `Void`：不关注返回类型时使用
- `Response<T>`：将响应适配成 `Retrofit.Response<T>` 对象返回
- `Mono<T>`：Project Reactor 响应式返回类型
- `Single<T>`：RxJava 响应式返回类型（支持 RxJava2/RxJava3）
- `Completable`：RxJava 响应式返回类型，用于 HTTP 请求无响应体的场景（支持 RxJava2/RxJava3）
- 任意 POJO 类型：将 Response Body 适配成对应的 POJO 对象返回

## 适配实现方式

Retrofit 底层通过 `CallAdapterFactory` 将 `Call<T>` 对象适配成接口方法的返回值类型，本组件扩展了以下 `CallAdapterFactory` 实现：

- **BodyCallAdapterFactory**
  - 同步执行 HTTP 请求，将响应体内容适配成方法的返回值类型
  - 任意方法返回值类型都可以使用，优先级最低

- **ResponseCallAdapterFactory**
  - 同步执行 HTTP 请求，将响应体内容适配成 `Retrofit.Response<T>` 返回
  - 只有方法返回值类型为 `Retrofit.Response<T>` 时才生效

- **响应式编程相关 CallAdapterFactory**
  - 支持 `Mono<T>`、`Single<T>`、`Completable` 等响应式类型

通过继承 `CallAdapter.Factory`，可以实现任何方式的 HTTP 响应到 Java 接口返回类型的适配。组件支持通过 `retrofit.global-call-adapter-factories` 配置全局调用适配器工厂：

```yaml
retrofit:
  # 全局适配器工厂（组件扩展的 CallAdapterFactory 已内置，请勿重复配置）
  global-call-adapter-factories:
    # ...
```

针对每个 Java 接口，还可以通过 `@RetrofitClient.callAdapterFactories` 指定当前接口采用的 `CallAdapter.Factory`。

---

[返回功能特性目录](../../README.md) | [下一节：自定义数据转换器](converter.md)