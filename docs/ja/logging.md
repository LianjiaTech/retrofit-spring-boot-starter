# ログ出力
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | **日本語** | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

コンポーネントはグローバルログ出力と宣言型ログ出力をサポートしています。

## グローバルログ出力

グローバルログ出力はデフォルトで無効（`enable=false`）であり、手動で有効化する必要があります。有効化後、デフォルトでは `BASIC` ストラテジーでリクエスト/レスポンス行のみ（ステータスコードと所要時間を含む）を出力し、オーバーヘッドは無視できる程度です。デフォルト設定は以下の通りです：

```yaml
retrofit:
  global-log:
    # ログ出力を有効化（デフォルト false）
    enable: false
    # グローバルログ出力レベル
    log-level: info
    # グローバルログ出力ストラテジー（デフォルト BASIC、リクエスト/レスポンス行のみ出力）
    log-strategy: basic
    # リクエストログを集約出力するか
    aggregate: true
    # ログ名、デフォルトは LoggingInterceptor の完全クラス名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # ログで隠すべき機密リクエストヘッダー
    # デフォルト遮蔽：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：ユーザーがこの項目を設定するとデフォルト値を全体オーバーライドするため、遮蔽を維持したい項目も含める必要があります
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

4つのログ出力ストラテジーの意味は以下の通りです：

1. **NONE**：ログを出力しない
2. **BASIC**：リクエストとレスポンス行のみ出力
3. **HEADERS**：リクエストとレスポンス行およびリクエストヘッダー/レスポンスヘッダーを出力
4. **BODY**：リクエストとレスポンス行、リクエストヘッダー/レスポンスヘッダーおよびリクエストボディ/レスポンスボディを出力（存在する場合）

## 宣言型ログ出力

一部のリクエストのみログ出力が必要な場合は、関連インターフェースまたはメソッドに `@Logging` アノテーションを使用できます。

## カスタム拡張

ログ出力動作を変更する必要がある場合は、`LoggingInterceptor` を継承し、Spring Bean として設定します。

---

[前へ：メソッドレベルタイムアウト設定](timeout.md) | [次へ：リクエストリトライ](retry.md)