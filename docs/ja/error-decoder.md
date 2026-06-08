# エラーデコーダ
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | **日本語** | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

HTTP リクエストエラー（例外の発生またはレスポンスデータが期待に合わない場合）が発生したとき、エラーデコーダは HTTP 関連情報をカスタム例外にデコードできます。

## 使用方法

`@RetrofitClient` アノテーションの `errorDecoder()` 属性で現在のインターフェースのエラーデコーダを指定します。カスタムエラーデコーダは `ErrorDecoder` インターフェースを実装する必要があります。

## ErrorDecoder の無効化

`retrofit.enable-error-decoder=false` を設定して ErrorDecoder 機能を無効化できます。

---

[前節：サーキットブレーカ / デグレード](degrade.md) | [次節：メトリクス監視（Micrometer）](metrics.md)