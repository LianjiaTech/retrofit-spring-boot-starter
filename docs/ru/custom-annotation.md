# Кастомные аннотации RetrofitClient
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | **Русский**

В некоторых случаях значения по умолчанию аннотаций `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` и др. на Java-интерфейсе не соответствуют бизнесовым потребностям. Один способ --修改ать соответствующие атрибуты аннотации для каждого интерфейса, но это приводит к тому, что многие интерфейсы должны执行овать одинаковую логику, что不够 элегантно. Другой способ -- создать кастомную аннотацию RetrofitClient, после чего другим интерфейсам只需要 использовать кастомную аннотацию.

Ниже определена кастомная аннотация `@MyRetrofitClient`, которая объединяет несколько аннотаций с значениями по умолчанию в одну аннотацию:

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

[Предыдущая: HTTP-вызовы между микросервисами](microservice.md) | [Следующая: Справочник конфигурации](configuration.md)