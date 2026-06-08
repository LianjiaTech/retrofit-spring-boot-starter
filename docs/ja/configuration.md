# 全設定項目リファレンス
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | **日本語** | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

コンポーネントは複数の設定可能な属性をサポートし、さまざまなビジネスシーンに対応します。以下は全設定属性とデフォルト値です：

```yaml
retrofit:
  # グローバルコンバータファクトリ（デフォルト JacksonConverterFactory）
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # グローバル適応ファクトリ（コンポーネント拡張の CallAdapterFactory は内蔵済み、重複設定しないでください）
  global-call-adapter-factories:
    # ...

  # グローバルログ出力設定
  global-log:
    # ログ出力を有効化（デフォルト false）
    enable: false
    # グローバルログ出力レベル
    log-level: info
    # グローバルログ出力ストラテジ（デフォルト BASIC）
    log-strategy: basic
    # リクエストログを集約して出力するか
    aggregate: true
    # ログ名。デフォルトは LoggingInterceptor の完全クラス名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # ログで隠す必要がある機密リクエストヘッダー
    # デフォルトでマスク：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：ユーザーがこの設定を構成するとデフォルト値全体がオーバーライドされます
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # グローバルリトライ設定
  global-retry:
    # グローバルリトライを有効にするか
    enable: false
    # グローバルリトライ基礎間隔時間（ミリ秒）
    interval-ms: 100
    # グローバル最大リトライ回数
    max-retries: 2
    # バックオフストラテジ：FIXED（固定間隔、デフォルト）/ EXPONENTIAL（指数バックオフ）
    backoff-strategy: fixed
    # 指数バックオフ間隔上限（ミリ秒）、EXPONENTIAL のみ有効
    max-interval-ms: 30000
    # ジッター係数 [0.0, 1.0]、0.0 はジッターなし
    jitter: 0.0
    # グローバルリトライルール
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # グローバルタイムアウト設定
  global-timeout:
    # グローバル読み取りタイムアウト時間（ミリ秒）
    read-timeout-ms: 10000
    # グローバル書き込みタイムアウト時間（ミリ秒）
    write-timeout-ms: 10000
    # グローバル接続タイムアウト時間（ミリ秒）
    connect-timeout-ms: 10000
    # グローバル完全呼び出しタイムアウト時間（ミリ秒）、0 はタイムアウトなし
    call-timeout-ms: 0

  # グローバル接続プール設定
  global-connection-pool:
    # 最大アイドル接続数
    max-idle-connections: 5
    # 接続維持時間（ミリ秒）
    keep-alive-duration-ms: 300000

  # メトリクス監視設定（デフォルト無効；enable=true の明示設定が必要。コンテナ内に MeterRegistry が必須）
  metrics:
    # 有効にするか、デフォルト false
    enable: false
    # Timer パーセンタイル
    percentiles: [0.5, 0.95, 0.99]
    # SLO ヒストグラムバケット
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # host タグを含めるか
      host: false
      # uri タグを含めるか
      uri: true
    # グローバル追加タグ
    extra-tags:
      app: my-service
    # メトリクス名プレフィックス
    metric-name-prefix: retrofit.client

  # サーキットブレーカ/デグレード設定
  degrade:
    # サーキットブレーカ/デグレードタイプ。デフォルト none、無効
    degrade-type: none
    # グローバル Sentinel デグレード設定
    global-sentinel-degrade:
      # 有効にするか
      enable: false
      rules:
        # デグレードストラテジ（0：平均レスポンス時間；1：例外比率；2：例外数）
        - grade: 0
          # 各デグレードストラテジに対応する閾値
          count: 1000
          # サーキットブレーカ時間（秒）
          time-window: 5
          # サーキットブレーカをトリガする最小リクエスト数
          min-request-amount: 5
          # RT モードでの低速リクエスト比率閾値
          slow-ratio-threshold: 1.0
          # 統計間隔時間（ミリ秒）
          stat-interval-ms: 1000
    # グローバル Resilience4j デグレード設定
    global-resilience4j-degrade:
      # 有効にするか
      enable: false
      # CircuitBreakerConfig を取得する名前。グローバルサーキットブレーカ設定として使用
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # PathMatchInterceptor の scope を prototype に自動設定
  auto-set-prototype-scope-for-path-math-interceptor: true
  # ErrorDecoder 機能を有効にするか
  enable-error-decoder: true
```

ほとんどのシーンでは、Spring Boot 設定ファイル（application.yml または application.properties）に上記設定を追加することで、コンポーネント機能をカスタマイズできます。設定が有効にならない問題がある場合は、[よくある質問](faq.md) を参照してください。

**Spring Boot 設定ファイルが有効にならない場合、RetrofitProperties Bean を手動で設定できます**。コードは以下の通りです：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties の各設定値を手動で変更
    return retrofitProperties;
}
```

---

[前節：RetrofitClient アノテーションのカスタマイズ](custom-annotation.md) | [次節：その他の機能例](examples.md)