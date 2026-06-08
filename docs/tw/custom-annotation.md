# 自定义 RetrofitClient 注解
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | **繁體中文** | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

有些时候，JAVA 介面上的 `@RetrofitClient`、`@Retry`、`@Logging`、`@Resilience4jDegrade` 等注解的預設值不符合業務需要。一種方式是每個介面都修改對應注解屬性，但会導致很多介面都要做相同的邏輯，不夠優雅。另一種方式就是自定义 RetrofitClient 注解，後續其他介面只需要使用自定义注解即可。

下面定義了自定义注解 `@MyRetrofitClient`，將多個注解的預設屬性合併為一個注解：

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

[上一節：微服务之间的 HTTP 调用](microservice.md) | [下一節：全量配置项参考](configuration.md)