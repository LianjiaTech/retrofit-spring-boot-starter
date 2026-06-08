# ログ出力
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | **日本語** | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

コンポーネントはグローバルログ出力と宣言型ログ出力をサポートしています。

## グローバルログ出力

グローバルログ出力はデフォルトで無効（`enable=false`）です。手動で有効化する必要があります。有効化後、デフォルトでは `BASIC` ストラテジでリクエスト/レスポンス行（ステータスコードと所要時間を含む）のみを出力し、オーバーヘッドは無視できる程度です。デフォルト設定は以下の通りです：

```yaml
retrofit:
  global-log:
    # ログ出力を有効化（デフォルト false）
    enable: false
    # グローバルログ出力レベル
    log-level: info
    # グローバルログ出力ストラテジ（デフォルト BASIC、リクエスト/レスポンス行のみ出力）
    log-strategy: basic
    # リクエストログを集約して出力するか
    aggregate: true
    # ログ名。デフォルトは LoggingInterceptor の完全クラス名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # ログで隠す必要がある機密リクエストヘッダー
    # デフォルトでマスク：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：ユーザーがこの設定を構成するとデフォルト値全体がオーバーライドされます。マスクしたい項目を含める必要があります
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

4つのログ出力ストラテジの意味は以下の通りです：

1. **NONE**：ログを出力しない
2. **BASIC**：リクエストとレスポンス行のみ出力
3. **HEADERS**：リクエストとレスポンス行およびリクエストヘッダー/レスポンスヘッダーを出力
4. **BODY**：リクエストとレスポンス行、リクエストヘッダー/レスポンスヘッダー、およびリクエストボディ/レスポンスボディ（存在する場合）を出力

## 宣言型ログ出力

一部のリクエストのみログを出力したい場合は、関連インターフェースまたはメソッドで `@Logging` アノテーションを使用できます。

## カスタム拡張

ログ出力動作を変更する必要がある場合は、`LoggingInterceptor` を継承し、Spring Bean として設定できます。

---

[前節：メソッドレベルのタイムアウト設定](timeout.md) | [次節：リクエストリトライ](retry.md)