# よくある質問
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | **日本語** | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration が自動ロードできない

一部の場面（`@SpringBootApplication(exclude = ...)` を使用する場合や XML 設定を混用するプロジェクト等）では、`RetrofitAutoConfiguration` が正常にロードできない可能性があります。この場合、手動で設定インポートを行えます：

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

プロジェクトが Spring XML 設定ファイルを依然使用している場合、XML 設定ファイルに Spring Boot 自動設定クラスを追加する必要があります：

```xml
<!-- Spring Boot 自動設定クラスをインポート -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 設定ファイルが反映されない

`application.yml` または `application.properties` の設定が反映されない場合、`RetrofitProperties` Bean を手動で設定できます：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties の各設定値を手動変更
    return retrofitProperties;
}
```

## プロパティ名 path-math とクラス名 PathMatchInterceptor の差異

設定プロパティ `auto-set-prototype-scope-for-path-math-interceptor` の `path-math` は歴史的命名であり、対応するインターセプタークラス名は `PathMatchInterceptor`（`match` を使用）です。これは既知の歴史的命名差異であり、機能の使用には影響しません。

## RetrofitClient スキャンパスの手動指定

デフォルトでは、コンポーネントは Spring Boot スキャンパスを使用して自動的に `RetrofitClient` を登録します。スキャンパスを手動指定する必要がある場合は、設定クラスに `@RetrofitScan` アノテーションを追加します。

## Jackson シリアライズ設定の変更

Jackson のシリアライズ/デシリアライズ動作をカスタマイズする必要がある場合は、`JacksonConverterFactory` の Spring Bean 設定を直接オーバーライド即可です。コンポーネントはデフォルトで `retrofit2.converter.jackson.JacksonConverterFactory` を使用し、Bean として登録後、カスタム Jackson `ObjectMapper` 設定が自動的に適用されます。

---

[機能一覧に戻る](../../README.md)