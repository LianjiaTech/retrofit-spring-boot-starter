# カスタム RetrofitClient アノテーション
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | **日本語** | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

時には、Java インターフェースの `@RetrofitClient`、`@Retry`、`@Logging`、`@Resilience4jDegrade` 等のアノテーションのデフォルト値がビジネスニーズに合わない場合があります。1つの方法は各インターフェースで対応アノテーション属性を変更することですが、多くのインターフェースで同じロジックを繰り返す必要があり、エレガントではありません。もう1つの方法はカスタム RetrofitClient アノテーションを定義し、以降の他のインターフェースはカスタムアノテーションのみを使用するようにします。

以下にカスタムアノテーション `@MyRetrofitClient` を定義し、複数のアノテーションのデフォルト属性を1つのアノテーションに統合します：

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

[前へ：マイクロサービス間の HTTP 呼び出し](microservice.md) | [次へ：全設定項目参考](configuration.md)