# Custom RetrofitClient Annotation
**English** | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

Sometimes, the default values of annotations such as `@RetrofitClient`, `@Retry`, `@Logging`, and `@Resilience4jDegrade` on Java interfaces do not meet business requirements. One approach is to modify the corresponding annotation attributes for each interface, but this results in many interfaces having to implement the same logic, which is not elegant. Another approach is to create a custom RetrofitClient annotation, so that other interfaces only need to use the custom annotation.

Below defines a custom annotation `@MyRetrofitClient` that merges the default attributes of multiple annotations into a single annotation:

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

[Previous: HTTP Calls Between Microservices](microservice.md) | [Next: Full Configuration Reference](configuration.md)