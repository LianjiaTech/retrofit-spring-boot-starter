# 全設定項目参考
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | **日本語** | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

コンポーネントは複数の設定可能プロパティをサポートし、様々なビジネス場面に対応します。以下に全設定プロパティとデフォルト値を示します：

```yaml
retrofit:
  # グローバル変換器ファクトリー（デフォルト JacksonConverterFactory）
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # グローバルアダプターファクトリー（コンポーネント拡張の CallAdapterFactory は内蔵済み、重複設定不可）
  global-call-adapter-factories:
    # ...

  # グローバルログ出力設定
  global-log:
    # ログ出力を有効化（デフォルト false）
    enable: false
    # グローバルログ出力レベル
    log-level: info
    # グローバルログ出力ストラテジー（デフォルト BASIC）
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

  # グローバルリトライ設定
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

  # グローバルタイムアウト設定
  global-timeout:
    # グローバル読取タイムアウト時間（ミリ秒）
    read-timeout-ms: 10000
    # グローバル書込タイムアウト時間（ミリ秒）
    write-timeout-ms: 10000
    # グローバル接続タイムアウト時間（ミリ秒）
    connect-timeout-ms: 10000
    # グローバル完全呼出タイムアウト時間（ミリ秒）、0 はタイムアウトなし
    call-timeout-ms: 0

  # グローバル接続プール設定
  global-connection-pool:
    # 最大アイドル接続数
    max-idle-connections: 5
    # 接続保持時間（ミリ秒）
    keep-alive-duration-ms: 300000

  # サーキットブレーカー/フェイルバック設定
  degrade:
    # サーキットブレーカー/フェイルバック型。デフォルト none、サーキットブレーカー/フェイルバックを無効
    degrade-type: none
    # グローバル Sentinel フェイルバック設定
    global-sentinel-degrade:
      # 有効化するか
      enable: false
      rules:
        # フェイルバックストラテジー（0：平均レスポンス時間；1：例外比率；2：例外数）
        - grade: 0
          # 各フェイルバックストラテジーに対応する閾値。平均レスポンス時間(ms)、例外比率(0-1)、例外数(1-N)
          count: 1000
          # サーキットブレーカー時間、単位 s
          time-window: 5
          # （有効統計時間範囲内）サーキットブレーカーをトリガーできる最小リクエスト数
          min-request-amount: 5
          # RT モードでの低速リクエスト率閾値
          slow-ratio-threshold: 1.0
          # 統計区間持続時間、単位ミリ秒
          stat-interval-ms: 1000
    # グローバル Resilience4j フェイルバック設定
    global-resilience4j-degrade:
      # 有効化するか
      enable: false
      # この名前で CircuitBreakerConfigRegistry から CircuitBreakerConfig を取得し、グローバルサーキットブレーカー設定として使用
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # PathMatchInterceptor の scope を prototype に自動設定
  auto-set-prototype-scope-for-path-math-interceptor: true
  # ErrorDecoder 機能を有効化するか
  enable-error-decoder: true
```

大部分の場面では、Spring Boot 設定ファイル（application.yml または application.properties）に上記設定を追加することで、コンポーネント機能をカスタマイズ変更できます。設定が反映されない等の問題がある場合は、[よくある質問](faq.md)を参照してください。

---

[前へ：カスタム RetrofitClient アノテーション](custom-annotation.md) | [次へ：その他機能例](examples.md)