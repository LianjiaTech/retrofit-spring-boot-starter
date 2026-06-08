# Actuator Endpoint（RetrofitClient メタ情報の公開）
[English](../en/actuator.md) | [简体中文](../cn/actuator.md) | [繁體中文](../tw/actuator.md) | **日本語** | [한국어](../ko/actuator.md) | [Español](../es/actuator.md) | [Türkçe](../tr/actuator.md) | [Русский](../ru/actuator.md)

コンポーネントは Spring Boot Actuator ベースの読み取り専用 Endpoint を提供し、`/actuator/retrofit` でアプリケーション内の全 `@RetrofitClient` インターフェースの完全な設定メタ情報を公開します。「あるインターフェースで実際に有効な baseUrl / タイムアウト / ログ / リトライ / サーキットブレーカ設定は何か」を調査しやすくします。

> **オプション依存関係、必要時に有効化**：ユーザーが actuator を導入した場合のみ Endpoint が構築されます（`@ConditionalOnClass`）。actuator を導入していない SpringBoot 3 プロジェクトには影響なく、正常に起動します。Endpoint の公開とオン/オフは Spring Boot の標準 management 設定（`@ConditionalOnAvailableEndpoint`）に完全に委ねられ、独自のスイッチは作成しません。

## 有効化方法

1. actuator を導入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. retrofit endpoint を公開（デフォルトでは actuator は `health` のみ公開。明示的に追加が必要）：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## アクセス方法

| リクエスト | 説明 |
|---|---|
| `GET /actuator/retrofit` | 全 client リスト + `global` グローバル設定セクション + `count` |
| `GET /actuator/retrofit/{インターフェース完全限定名}` | インターフェース完全限定名で単一 client を検索。未一致の場合は 404 |

## レスポンス構造例

```json
{
  "count": 2,
  "global": {
    "enableErrorDecoder": true,
    "globalConverterFactories": ["retrofit2.converter.jackson.JacksonConverterFactory"],
    "timeout": { "connectMs": 10000, "readMs": 10000, "writeMs": 10000, "callMs": 0 },
    "connectionPool": { "maxIdleConnections": 5, "keepAliveDurationMs": 300000 },
    "log":     { "enable": false, "logLevel": "INFO", "logStrategy": "BASIC", "aggregate": true },
    "retry":   { "enable": false, "maxRetries": 2, "intervalMs": 100,
                 "backoffStrategy": "FIXED", "maxIntervalMs": 30000, "jitter": 0.0,
                 "retryStatusCodes": [], "retryExceptionClasses": [],
                 "retryRules": ["RESPONSE_STATUS_NOT_2XX", "OCCUR_IO_EXCEPTION"] },
    "degrade": { "degradeType": "none",
                 "sentinel":     { "enable": false, "ruleCount": 0 },
                 "resilience4j": { "enable": false, "circuitBreakerConfigName": "defaultCircuitBreakerConfig" } },
    "metrics": { "enable": false, "metricNamePrefix": "retrofit.client", "tagHost": false, "tagUri": true }
  },
  "clients": [{
    "beanName": "userService",
    "interfaceName": "com.example.UserService",
    "baseUrl": "${test.baseUrl}",
    "resolvedBaseUrl": "http://localhost:8080/api/user/",
    "serviceId": null,
    "path": null,
    "converterFactories": [],
    "callAdapterFactories": [],
    "fallback": null,
    "fallbackFactory": null,
    "errorDecoder": "com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder$DefaultErrorDecoder",
    "validateEagerly": false,
    "sourceOkHttpClient": null,
    "timeoutEffective": true,
    "timeout": { "connectMs": 3000, "readMs": 3000, "writeMs": 10000, "callMs": 0,
                 "inheritedFields": ["writeMs", "callMs"] },
    "pool":    { "maxIdleConnections": 5, "keepAliveDurationMs": 300000,
                 "inheritedFields": ["maxIdleConnections", "keepAliveDurationMs"] },
    "logging": { "source": "interface", "enable": true, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## フィールド意味の説明

- **`resolvedBaseUrl`**：解決済みの最終 baseUrl。インターフェースが注入使用済み（インスタンス化トリガ済み）の場合のみ値があり、それ以外は `null`（baseUrl は遅延解決、未トリガ時は事前解決しない）。
- **`timeout` / `pool` の `inheritedFields`**：`@RetrofitClient` の対応フィールド設定値 `-1`（デフォルト値）は「グローバル設定を再利用」を意味します。Endpoint は実際の構築と一致するルールで `-1` をグローバルフォールバック値に解決し、これらのフィールド名を `inheritedFields` に記録し、「インターフェース明示設定」と「グローバル継承」の区別に利用します。
- **`timeoutEffective`**：インターフェースが `sourceOkHttpClient` でカスタム OkHttpClient を指定している場合 `false`（この場合、タイムアウト/接続プールはソースクライアントで決定、`timeout`/`pool` は表示されない）。
- **`logging` / `retry` の `source`**：
  - `"interface"`：インターフェースに `@Logging` / `@Retry` アノテーションがあり、他のフィールドはアノテーション展開値；
  - `"global"`：インターフェースに対応アノテーションがなく、ランタイムでグローバル設定にフォールバック。この場合値は重複展開されず、トップレベル `global` セクションを参照してください。
  - 注意：メソッドレベル `@Logging` / `@Retry` はここではドリルダウン表示されません（ランタイムではメソッドアノテーションがインターフェース、インターフェースがグローバルに優先）。
- **`degrade.enabled`**：`RetrofitDegrade.isEnableDegrade(インターフェース)` から取得；`type` はグローバル `degrade.degrade-type`（`none` / `sentinel` / `resilience4j`）。
- **`fallback` / `fallbackFactory`**：未設定（デフォルト `void.class`）の場合 `null`。

---

[前節：メトリクス監視（Micrometer）](metrics.md) | [次節：GraalVM Native Image / AOT サポート](aot.md)