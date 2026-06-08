# HTTP 回應結果自動適配
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | **繁體中文** | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

本元件會將 HTTP 回應結果自動適配成 Java 介面定義的返回類型，目前支援以下幾種返回類型：

- `Call<T>`：不執行適配處理，直接返回 `Call<T>` 物件
- `String`：將 Response Body 適配成 `String` 返回
  - 預設使用 JSON Converter 將 Response Body 的 bytes 轉成 String；如果想直接得到 Response Body 轉成的 String，可以指定 `Converter.Factory` 為 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- 基礎類型（`Long`/`Integer`/`Boolean`/`Float`/`Double`）：將 Response Body 適配成對應基礎類型
- `CompletableFuture<T>`：將 Response Body 適配成 `CompletableFuture<T>` 物件返回
- `Void`：不關注返回類型時使用
- `Response<T>`：將回應適配成 `Retrofit.Response<T>` 物件返回
- `Mono<T>`：Project Reactor 回應式返回類型
- `Single<T>`：RxJava 回應式返回類型（支援 RxJava2/RxJava3）
- `Completable`：RxJava 回應式返回類型，用於 HTTP 請求無回應體的場景（支援 RxJava2/RxJava3）
- 任意 POJO 類型：將 Response Body 適配成對應的 POJO 物件返回

## 適配實作方式

Retrofit 底層透過 `CallAdapterFactory` 將 `Call<T>` 物件適配成介面方法的返回值類型，本元件擴充了以下 `CallAdapterFactory` 實作：

- **BodyCallAdapterFactory**
  - 同步執行 HTTP 請求，將回應體內容適配成方法的返回值類型
  - 任意方法返回值類型都可以使用，優先級最低

- **ResponseCallAdapterFactory**
  - 同步執行 HTTP 請求，將回應體內容適配成 `Retrofit.Response<T>` 返回
  - 只有方法返回值類型為 `Retrofit.Response<T>` 時才生效

- **回應式程式設計相關 CallAdapterFactory**
  - 支援 `Mono<T>`、`Single<T>`、`Completable` 等回應式類型

透過繼承 `CallAdapter.Factory`，可以實作任何方式的 HTTP 回應到 Java 介面返回類型的適配。元件支援透過 `retrofit.global-call-adapter-factories` 設定全域呼叫適配器工廠：

```yaml
retrofit:
  # 全域適配器工廠（元件擴充的 CallAdapterFactory 已內建，請勿重複設定）
  global-call-adapter-factories:
    # ...
```

針對每個 Java 介面，還可以透過 `@RetrofitClient.callAdapterFactories` 指定當前介面採用的 `CallAdapter.Factory`。

---

[返回功能特性目錄](../README.md) | [下一節：自訂資料轉換器](converter.md)