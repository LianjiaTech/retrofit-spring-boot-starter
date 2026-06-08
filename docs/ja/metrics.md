# メトリクス監視（Micrometer）
[English](../en/metrics.md) | [简体中文](../cn/metrics.md) | [繁體中文](../tw/metrics.md) | **日本語** | [한국어](../ko/metrics.md) | [Español](../es/metrics.md) | [Türkçe](../tr/metrics.md) | [Русский](../ru/metrics.md)

コンポーネントは [Micrometer](https://micrometer.io/) ベースのメトリクス収集機能を内蔵しています。**デフォルトで無効**であり、`retrofit.metrics.enable=true` を明示的に設定する必要があります。

> **デフォルト無効・明示的有効化の理由**：Spring Boot autoconfig 間に可靠的なロード順序制約がなく、`@ConditionalOnBean(MeterRegistry.class)` に依存する自動有効化は評価时机の問題により「actuator を導入したのにメトリクスがない」という潜在的な失敗を引き起こします。opt-in に変更後、動作は完全に予測可能になります：actuator を導入しても自動的に instrumentation されません。明示的に有効化した時にコンテナ内に `MeterRegistry` がない場合、起動時に快速失敗となり、静默無メトリクスではなくなります。

## 有効化方法

1. Micrometer と対応する監視バックエンド（Prometheus / Datadog / Atlas 等）を導入します。Spring Boot Actuator は `MeterRegistry` を登録します：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. 設定で明示的に有効化します：

```yaml
retrofit:
  metrics:
    enable: true
```

## 収集されるメトリクス

| メトリクス名 | タイプ | 意味 |
|---|---|---|
| `retrofit.client.requests` | Timer | 各 HTTP 呼び出しの所要時間分布（パーセンタイルと SLO ヒストグラムを含む） |
| `retrofit.client.requests.active` | LongTaskTimer | 実行中のリクエスト数と最大存活時間 |
| `retrofit.client.errors` | Counter | リクエスト例外カウント（exception クラス名ディメンション別） |

## タグディメンション

デフォルト tag（基数有界、Prometheus 等の高基数敏感バックエンドで安全に使用可能）：

| Tag | 意味 | 値の例 |
|---|---|---|
| `client` | Retrofit インターフェースの単純クラス名 | `UserService` |
| `method` | Java メソッド名 | `getUser` |
| `http.method` | HTTP メソッド | `GET`/`POST` |
| `uri` | アノテーション上のパステンプレート（`@Path` を展開しない） | `user/{id}` |
| `status` | ステータスコードバケット | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | ビジネス結果 | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | errors メトリクスのみ、例外クラス名 | `SocketTimeoutException` |

> **注意**：tag 値は有界集合である必要があります。したがって、`uri` タグはアノテーション上のパステンプレート（`{id}` プレースホルダーを含む）を使用し、展開後の実際の URL ではありません。これにより、動的パスパラメータによるメトリクス基数の爆発を防止できます。

## 設定項目

```yaml
retrofit:
  metrics:
    # 有効にするか。デフォルト false。true に設定するとメトリクスインターセプタが構築されます
    enable: true
    # Timer が発行するパーセンタイル。空配列は発行しない
    percentiles: [0.5, 0.95, 0.99]
    # SLO ヒストグラムバケット。空配列はヒストグラムを発行しない
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # host タグを含めるか。デフォルト無効（動的 baseUrl シーンでは host 数が大きくなる可能性）
      host: false
      # uri タグを含めるか。デフォルト有効
      uri: true
    # グローバル静的追加タグ
    extra-tags:
      app: my-service
      env: prod
    # メトリクス名プレフィックス。デフォルト retrofit.client
    metric-name-prefix: retrofit.client
```

## カスタムタグ

デフォルトの tag ディメンションが要件を満たさない場合、`RetrofitTagsProvider` インターフェースを実装して Spring Bean として登録すると、デフォルト実装が自動的にオーバーライドされます：

```java
@Component
public class TenantAwareTagsProvider implements RetrofitTagsProvider {

    private final RetrofitTagsProvider delegate;

    public TenantAwareTagsProvider(MetricsProperty property) {
        this.delegate = new DefaultRetrofitTagsProvider(property);
    }

    @Override
    public Tags getTags(Request request, Response response, Throwable exception) {
        return delegate.getTags(request, response, exception)
                .and("tenant", TenantContext.current());
    }
}
```

> カスタム実装時は、tag 値集合が有界であること、tag順序と名前が安定であることを必ず保証してください。否则、Micrometer が複数の無意味な Meter を作成し、メモリ浪費を引き起こします。

---

[前節：エラーデコーダ](error-decoder.md) | [次節：Actuator Endpoint](actuator.md)