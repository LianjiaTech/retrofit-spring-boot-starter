# 自定义 RetrofitClient 注解
[English](../en/custom-annotation.md) | **简体中文** | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

有些时候，Java 接口上的 `@RetrofitClient`、`@Retry`、`@Logging`、`@Resilience4jDegrade` 等注解的默认值不符合业务需要。一种方式是每个接口都修改对应注解属性，但会导致很多接口都要做相同的逻辑，不够优雅。另一种方式就是自定义 RetrofitClient 注解，后续其他接口只需要使用自定义注解即可。

下面定义了自定义注解 `@MyRetrofitClient`，将多个注解的默认属性合并为一个注解：

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

[上一节：微服务之间的 HTTP 调用](microservice.md) | [下一节：全量配置项参考](configuration.md)