# よくある質問
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | **日本語** | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration が自動ロードされない

一部のシーン（`@SpringBootApplication(exclude = ...)` を使用、または XML 設定と混用するプロジェクト等）では、`RetrofitAutoConfiguration` が正常にロードされない可能性があります。この場合、手動で設定をインポートできます：

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

プロジェクトが Spring XML 設定ファイルをまだ使用している場合、XML 設定ファイルに Spring Boot 自動設定クラスを追加する必要があります：

```xml
<!-- Spring Boot 自動設定クラスをインポート -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 設定ファイルが有効にならない

`application.yml` または `application.properties` の設定が有効にならない場合、`RetrofitProperties` Bean を手動で設定できます：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties の各設定値を手動で変更
    return retrofitProperties;
}
```

## プロパティ名 path-math とクラス名 PathMatchInterceptor の違い

設定プロパティ `auto-set-prototype-scope-for-path-math-interceptor` の `path-math` は歴史的命名であり、対応するインターセプタクラス名は `PathMatchInterceptor`（`match` を使用）です。これは既知の歴史的命名差異であり、機能使用に影響はありません。

## RetrofitClient スキャンパスの手動指定

デフォルトでは、コンポーネントは Spring Boot スキャンパスを使用して `RetrofitClient` を登録します。スキャンパスを手動で指定する必要がある場合は、設定クラスに `@RetrofitScan` アノテーションを追加してください。

## Jackson シリアライゼーション設定の変更

Jackson のシリアライゼーション/デシリアライゼーション動作をカスタマイズする必要がある場合は、`JacksonConverterFactory` の Spring Bean 設定を直接オーバーライドしてください。コンポーネントはデフォルトで `retrofit2.converter.jackson.JacksonConverterFactory` を使用し、これを Bean として登録すると、カスタム Jackson `ObjectMapper` 設定が自動的に適用されます。

---

[機能特性目次に戻る](../ja/README.md)