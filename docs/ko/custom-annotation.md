# 커스텀 RetrofitClient 어노테이션
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | **한국어** | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

有些时, Java 인터페이스의 `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` 등 어노테이션의 기본값이 비즈니스需求에 부합하지 않을 수 있습니다. 한 가지 방법은每个 인터페이스에서 해당 어노테이션 속성을 수정하는 것이지만, 많은 인터페이스에서 동일한 로직을 수행해야 하므로优雅하지 않습니다. 다른 방법은 커스텀 RetrofitClient 어노테이션을 정의하는 것입니다, 이후 다른 인터페이스는 커스텀 어노테이션만 사용하면 됩니다.

다음은 커스텀 어노테이션 `@MyRetrofitClient`를 정의하여, 여러 어노테이션의 기본 속성을 하나의 어노테이션으로合并하는 예시입니다:

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

[이전: 마이크로서비스간 HTTP 호출](microservice.md) | [다음: 전체 설정 항목 참조](configuration.md)