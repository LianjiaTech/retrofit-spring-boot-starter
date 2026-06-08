# Пользовательская аннотация RetrofitClient
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | [Español](../es/custom-annotation.md) | [Türkçe](../tr/custom-annotation.md) | **Русский**

Иногда значения по умолчанию аннотаций `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` и других на Java-интерфейсах не соответствуют бизнес-потребностям. Один подход — изменять соответствующие свойства аннотаций для каждого интерфейса, но это приводит к повторению одинаковой логики на многих интерфейсах, что неэлегантно. Другой подход — создать пользовательскую аннотацию RetrofitClient, после чего другим интерфейсам нужно только использовать пользовательскую аннотацию.

Ниже определена пользовательская аннотация `@MyRetrofitClient`, объединяющая свойства по умолчанию нескольких аннотаций в одну:

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

Предыдущий: [HTTP-вызовы между микросервисами](microservice.md) | Следующий: [Справочник всех конфигураций](configuration.md)