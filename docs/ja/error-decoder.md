# エラーデコーダー
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | **日本語** | [한국어](../ko/error-decoder.md) | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

HTTP リクエストエラー（例外発生またはレスポンスデータが期待に合わない場合）が発生した際、エラーデコーダーは HTTP 関連情報をカスタム例外にデコードできます。

## 使用方法

`@RetrofitClient` アノテーションの `errorDecoder()` 属性で現在のインターフェースのエラーデコーダーを指定します。カスタムエラーデコーダーは `ErrorDecoder` インターフェースを実装する必要があります。

## ErrorDecoder の無効化

`retrofit.enable-error-decoder=false` を設定することで ErrorDecoder 機能を無効化できます。

---

[前へ：サーキットブレーカー/フェイルバック](degrade.md) | [次へ：マイクロサービス間の HTTP 呼び出し](microservice.md)