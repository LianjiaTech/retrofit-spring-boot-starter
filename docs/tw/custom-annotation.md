# 自訂 RetrofitClient 註解
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | **繁體中文** | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

有些時候，Java 介面上的 `@RetrofitClient`、`@Retry`、`@Logging`、`@Resilience4jDegrade` 等註解的預設值不符合業務需要。一種方式是每個介面都修改對應註解屬性，但會導致很多介面都要做相同的邏輯，不夠優雅。另一種方式就是自訂 RetrofitClient 註解，後續其他介面只需要使用自訂註解即可。

下面定義了自訂註解 `@MyRetrofitClient`，將多個註解的預設屬性合併為一個註解：

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

[上一節：微服務之間的 HTTP 呼叫](microservice.md) | [下一節：全量設定項參考](configuration.md)