# GraalVM Native Image / AOT サポート
[English](../en/aot.md) | [简体中文](../cn/aot.md) | [繁體中文](../tw/aot.md) | **日本語** | [한국어](../ko/aot.md) | [Español](../es/aot.md) | [Türkçe](../tr/aot.md) | [Русский](../ru/aot.md)

コンポーネントは Spring AOT サポートを内蔵しており、Spring Boot 3.x / 4.x で GraalVM Native Image にコンパイルする際に**そのまま使用可能。`reflect-config.json` / `proxy-config.json` を手書きする必要はありません**。

構築期（`spring-boot:process-aot` または native コンパイル）では、各 `@RetrofitClient` インターフェースに対して以下が自動登録されます：

- **JDK ダイナミックプロキシ**：`Retrofit.create(インターフェース)` とサーキットブレーカ/デグレードプロキシはインターフェースベースで JDK プロキシを生成；
- **インターフェースリフレクション**：メソッドシグネチャとパラメータアノテーションは native 下でリフレクション可視である必要があり、Retrofit が HTTP リクエストを解析するために使用；
- **アノテーション参照クラスのリフレクション構築**：`@RetrofitClient` 上の `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory`、および `@InterceptMark`（`@Intercept` / `@Sign` を含む）アノテーションの `handler` インターセプタクラス。ランタイムでリフレクションにより作成・注入される可能性があります；
- **Actuator 値オブジェクトのシリアライゼーション**：`/actuator/retrofit` レスポンス結果のリフレクションシリアライゼーション。

> この機能は `RetrofitAotProcessor`（`BeanFactoryInitializationAotProcessor`）で実装されています。**AOT 構築期のみ有効**であり、通常の JVM 起動と native ランタイムではロジックが実行されず、機能とパフォーマンスにゼロ影響です。
>
> カスタム `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` 等が JSON シリアライゼーションで複雑なビジネスエンティティになる場合、ビジネスエンティティ自体の native リフレクション hints は Spring の標準方法（例：`@RegisterReflectionForBinding`）で宣言する必要があります -- これは具体的なビジネスモデルに関連し、コンポーネントの責任範囲外です。

---

[前節：Actuator Endpoint](actuator.md) | [次節：マイクロサービス間の HTTP 呼び出し](microservice.md)