# RetrofitClient 어노테이션 커스터마이징
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | **한국어** | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

場合에 따라 Java 인터페이스의 `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` 등 어노테이션의 기본값이 비즈니스 요구에 맞지 않을 수 있습니다. 한 가지 방법은 각 인터페이스에서 해당 어노테이션 속성을 수정하는 것이지만, 많은 인터페이스에서 같은 로직을 반복하게 되어优雅하지 않습니다. 다른 방법은 커스텀 RetrofitClient 어노테이션을 정義하고, 이후 다른 인터페이스는 커스텀 어노테이션만 사용하는 것입니다.

다음 예에서는 커스텀 어노테이션 `@MyRetrofitClient`를 정의하여, 여러 어노테이션의 기본 속성을 하나의 어노테이션으로 통합합니다:

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

[이전 섹션: 마이크로서비스 간 HTTP 호출](microservice.md) | [다음 섹션: 전체 설정 항목 레퍼런스](configuration.md)