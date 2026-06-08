# HTTP レスポンス結果の自動適応
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | **日本語** | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

本コンポーネントは HTTP レスポンス結果を Java インターフェースで定義された戻り値型に自動適応します。現在、以下の戻り値型をサポートしています：

- `Call<T>`：適応処理を行わず、`Call<T>` オブジェクトを直接返す
- `String`：Response Body を `String` に適応して返す
  - デフォルトでは JSON Converter を使用して Response Body の bytes を String に変換します。Response Body から直接 String を取得したい場合は、`Converter.Factory` として `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` を指定してください
- 基本型（`Long`/`Integer`/`Boolean`/`Float`/`Double`）：Response Body を対応する基本型に適応
- `CompletableFuture<T>`：Response Body を `CompletableFuture<T>` オブジェクトに適応して返す
- `Void`：戻り値型を気にしない場合に使用
- `Response<T>`：レスポンスを `Retrofit.Response<T>` オブジェクトに適応して返す
- `Mono<T>`：Project Reactor のリアクティブ戻り値型
- `Single<T>`：RxJava のリアクティブ戻り値型（RxJava2/RxJava3 をサポート）
- `Completable`：RxJava のリアクティブ戻り値型。HTTP リクエストにレスポンスボディがない場合に使用（RxJava2/RxJava3 をサポート）
- 任意の POJO 型：Response Body を対応する POJO オブジェクトに適応して返す

## 適応の実装方式

Retrofit は底层で `CallAdapterFactory` を使用して `Call<T>` オブジェクトをインターフェースメソッドの戻り値型に適応します。本コンポーネントは以下の `CallAdapterFactory` 実装を拡張しています：

- **BodyCallAdapterFactory**
  - HTTP リクエストを同期的に実行し、レスポンスボディの内容をメソッドの戻り値型に適応
  - 任意のメソッド戻り値型に使用可能。最低優先度

- **ResponseCallAdapterFactory**
  - HTTP リクエストを同期的に実行し、レスポンスボディの内容を `Retrofit.Response<T>` に適応して返す
  - メソッドの戻り値型が `Retrofit.Response<T>` の場合のみ有効

- **リアクティブプログラミング関連 CallAdapterFactory**
  - `Mono<T>`、`Single<T>`、`Completable` 等のリアクティブ型をサポート

`CallAdapter.Factory` を継承することで、任意の方式で HTTP レスポンスを Java インターフェースの戻り値型に適応させることができます。コンポーネントは `retrofit.global-call-adapter-factories` 設定でグローバル呼び出し適応ファクトリをサポートしています：

```yaml
retrofit:
  # グローバル適応ファクトリ（コンポーネント拡張の CallAdapterFactory は内蔵済み、重複設定しないでください）
  global-call-adapter-factories:
    # ...
```

各 Java インターフェースでは、`@RetrofitClient.callAdapterFactories` で現在のインターフェースに使用する `CallAdapter.Factory` を指定することもできます。

---

[機能特性目次に戻る](../ja/README.md) | [次節：データコンバータのカスタマイズ](converter.md)