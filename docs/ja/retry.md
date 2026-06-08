# リクエストリトライ
[English](../en/retry.md) | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | **日本語** | [한국어](../ko/retry.md) | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

コンポーネントはグローバルリトライと宣言型リトライをサポートしています。

## グローバルリトライ

グローバルリトライはデフォルトで無効です。デフォルト設定項目は以下の通りです：

```yaml
retrofit:
  global-retry:
    # グローバルリトライを有効化するか
    enable: false
    # グローバルリトライ基礎間隔時間（ミリ秒）
    interval-ms: 100
    # グローバル最大リトライ回数
    max-retries: 2
    # バックオフストラテジー：FIXED（固定間隔、デフォルト）/ EXPONENTIAL（指数バックオフ）
    backoff-strategy: fixed
    # 指数バックオフ間隔上限（ミリ秒）、EXPONENTIAL の場合のみ有効
    max-interval-ms: 30000
    # ジッター係数 [0.0, 1.0]、0.0 はジッターなし
    jitter: 0.0
    # グローバルリトライルール
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### リトライルール

リトライルールは3つの設定をサポートしています：

1. **RESPONSE_STATUS_NOT_2XX**：レスポンスステータスコードが 2xx でない場合にリトライを実行
2. **OCCUR_IO_EXCEPTION**：IO 例外が発生した場合にリトライを実行
3. **OCCUR_EXCEPTION**：任意の例外が発生した場合にリトライを実行

### バックオフストラテジーとジッター

`backoffStrategy` はリトライ間隔の増加方式を制御します。デフォルト `FIXED` は過去の動作と一致します：

- **FIXED**：各リトライ間隔は固定で `intervalMs`
- **EXPONENTIAL**：指数バックオフ、第 N 回リトライ間隔 = `intervalMs × 2^N`（N は 0 から開始）、`maxIntervalMs` で上限を設定し、間隔の無限増大を防ぐ

`jitter`（値 `[0.0, 1.0]`、デフォルト `0.0` でジッターなし）は計算遅延にランダムジッターを追加し、複数クライアントの同時リトライによるスタンピード効果を防ぎます：

> 実際の遅延 = 計算遅延 × (1 + jitter × random)、ここで random は `[0, 1)` の乱数

### 条件トリガー：ステータスコード / 例外型による

`RetryRule` の粒度の粗いルールに基づき、さらにトリガー条件を絞り込むことができます（デフォルト空、過去の動作と一致）：

- `retryStatusCodes`：レスポンスステータスコードがリストに一致する場合のみリトライ（`RESPONSE_STATUS_NOT_2XX` ルールと併用必要）。例：`{502, 503, 504}`
- `retryExceptionClasses`：例外型がリストに一致する場合のみリトライ（`RetryRule` に一致する例外の上でさらに絞り込み）。例：`{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 宣言型リトライ

一部のリクエストのみリトライが必要な場合は、対応するインターフェースまたはメソッドに `@Retry` アノテーションを使用できます。

## カスタム拡張

リクエストリトライ動作を変更する必要がある場合は、`RetryInterceptor` を継承し、Spring Bean として設定します。

---

[前へ：ログ出力](logging.md) | [次へ：インターセプター](interceptor.md)