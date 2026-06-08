# HTTP 响应结果自动适配
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | **繁體中文** | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

本元件會將 HTTP 响应结果自動适配成 JAVA 介面定義的回傳型別，目前支援以下幾種回傳型別：

- `Call<T>`：不執行适配處理，直接回傳 `Call<T>` 物件
- `String`：將 Response Body 适配成 `String` 回傳
  - 預設使用 JSON Converter 將 Response Body 的 bytes 轉成 String；如果想直接得到 Response Body 轉成的 String，可以指定 `Converter.Factory` 為 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- 基礎型別（`Long`/`Integer`/`Boolean`/`Float`/`Double`）：將 Response Body 适配成對應基礎型別
- `CompletableFuture<T>`：將 Response Body 适配成 `CompletableFuture<T>` 物件回傳
- `Void`：不關注回傳型別時使用
- `Response<T>`：將响应适配成 `Retrofit.Response<T>` 物件回傳
- `Mono<T>`：Project Reactor 响应式回傳型別
- `Single<T>`：RxJava 响应式回傳型別（支援 RxJava2/RxJava3）
- `Completable`：RxJava 响应式回傳型別，用於 HTTP 請求無响应體的場景（支援 RxJava2/RxJava3）
- 任意 POJO 型別：將 Response Body 适配成對應的 POJO 物件回傳

## 适配實作方式

Retrofit 底層透過 `CallAdapterFactory` 將 `Call<T>` 物件适配成介面方法的回傳值型別，本元件擴展了以下 `CallAdapterFactory` 實作：

- **BodyCallAdapterFactory**
  - 同步執行 HTTP 請求，將响应體內容适配成方法的回傳值型別
  - 任意方法回傳值型別都可以使用，優先級最低

- **ResponseCallAdapterFactory**
  - 同步執行 HTTP 請求，將响应體內容适配成 `Retrofit.Response<T>` 回傳
  - 只有方法回傳值型別為 `Retrofit.Response<T>` 時才生效

- **响应式程式設計相關 CallAdapterFactory**
  - 支援 `Mono<T>`、`Single<T>`、`Completable` 等响应式型別

透過繼承 `CallAdapter.Factory`，可以實作任何方式的 HTTP 响应到 JAVA 介面回傳型別的适配。元件支援透過 `retrofit.global-call-adapter-factories` 配置全域呼叫适配器工廠：

```yaml
retrofit:
  # 全域适配器工廠（元件擴展的 CallAdapterFactory 已內建，請勿重複配置）
  global-call-adapter-factories:
    # ...
```

針對每個 JAVA 介面，還可以透過 `@RetrofitClient.callAdapterFactories` 指定當前介面採用的 `CallAdapter.Factory`。

---

[返回功能特性目錄](../tw/README.md) | [下一節：自定义数据转换器](converter.md)