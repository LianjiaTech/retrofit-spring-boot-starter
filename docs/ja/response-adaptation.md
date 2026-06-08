# HTTP レスポンス自動適応
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | **日本語** | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

本コンポーネントは HTTP レスポンスを Java インターフェース定義の戻り値型に自動適応します。現在以下の戻り値型をサポートしています：

- `Call<T>`：適応処理を行わず、直接 `Call<T>` オブジェクトを返す
- `String`：レスポンスボディを `String` に適応して返す
  - デフォルトでは JSON Converter を使用してレスポンスボディの bytes を String に変換；レスポンスボディを直接 String として取得したい場合は、`Converter.Factory` に `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` を指定
- 基本型（`Long`/`Integer`/`Boolean`/`Float`/`Double`）：レスポンスボディを対応する基本型に適応
- `CompletableFuture<T>`：レスポンスボディを `CompletableFuture<T>` オブジェクトに適応して返す
- `Void`：戻り値型を意識しない場合に使用
- `Response<T>`：レスポンスを `Retrofit.Response<T>` オブジェクトに適応して返す
- `Mono<T>`：Project Reactor リアクティブ戻り値型
- `Single<T>`：RxJava リアクティブ戻り値型（RxJava2/RxJava3 対応）
- `Completable`：RxJava リアクティブ戻り値型、HTTP リクエストにレスポンスボディがない場面に使用（RxJava2/RxJava3 対応）
- 任意の POJO 型：レスポンスボディを対応する POJO オブジェクトに適応して返す

## 適応実装方式

Retrofit は基盤で `CallAdapterFactory` を通じて `Call<T>` オブジェクトをインターフェースメソッドの戻り値型に適応します。本コンポーネントは以下の `CallAdapterFactory` 実装を拡張しています：

- **BodyCallAdapterFactory**
  - HTTP リクエストを同期的に実行し、レスポンスボディの内容をメソッドの戻り値型に適応
  - 任意のメソッド戻り値型に使用可能、優先度は最低

- **ResponseCallAdapterFactory**
  - HTTP リクエストを同期的に実行し、レスポンスボディの内容を `Retrofit.Response<T>` に適応して返す
  - メソッドの戻り値型が `Retrofit.Response<T>` の場合のみ有効

- **リアクティブプログラミング関連 CallAdapterFactory**
  - `Mono<T>`、`Single<T>`、`Completable` 等のリアクティブ型をサポート

`CallAdapter.Factory` を継承することで、HTTP レスポンスから Java インターフェース戻り値型への任意の適応方式を実装できます。コンポーネントは `retrofit.global-call-adapter-factories` 設定でグローバルコールアダプターファクトリーをサポートしています：

```yaml
retrofit:
  # グローバルアダプターファクトリー（コンポーネント拡張の CallAdapterFactory は内蔵済み、重複設定不可）
  global-call-adapter-factories:
    # ...
```

各 Java インターフェースに対して、`@RetrofitClient.callAdapterFactories` で現在のインターフェースで使用する `CallAdapter.Factory` を指定することも可能です。

---

[機能一覧に戻る](../../README.md) | [次へ：カスタムデータ変換器](converter.md)