# RetrofitClient アノテーションのカスタマイズ
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | **日本語** | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

場合によっては、Java インターフェース上の `@RetrofitClient`、`@Retry`、`@Logging`、`@Resilience4jDegrade` 等のアノテーションのデフォルト値がビジネス要件に合わないことがあります。1つの方法は各インターフェースでアノテーション属性を変更することですが、多くのインターフェースで同じロジックを繰り返すことになり、優雅ではありません。別の方法はカスタム RetrofitClient アノテーションを定義し、以降の他のインターフェースはカスタムアノテーションを使用するだけです。

以下の例では、カスタムアノテーション `@MyRetrofitClient` を定義し、複数のアノテーションのデフォルト属性を1つのアノテーションに統合しています：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Logging(logLevel = LogLevel.WARN)
@Retry(intervalMs = 200)
public @interface MyRetrofitClient {

    @AliasFor(annotation = RetrofitClient.class, attribute = "converterFactories")
    Class<? extends Converter.Factory>[] converterFactories() default {GsonConverterFactory.class};

    @AliasFor(annotation = Logging.class, attribute = "logStrategy")
    LogStrategy logStrategy() default LogStrategy.BODY;
}
```

---

[前節：マイクロサービス間の HTTP 呼び出し](microservice.md) | [次節：全設定項目リファレンス](configuration.md)