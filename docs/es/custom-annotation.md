# Anotación RetrofitClient personalizada
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | **Español** | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

En algunas ocasiones, los valores predeterminados de las anotaciones `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade` en la interfaz Java no cumplen con las necesidades del negocio. Una opción es modificar las propiedades de la anotación correspondiente en cada interfaz, pero esto causa que muchas interfaces deban implementar la misma lógica, lo cual no es elegante. La otra opción es personalizar la anotación RetrofitClient, de modo que las demás interfaces solo necesitan usar la anotación personalizada.

A continuación se define la anotación personalizada `@MyRetrofitClient`, fusionando las propiedades predeterminadas de múltiples anotaciones en una sola anotación:

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

[Anterior: Llamadas HTTP entre microservicios](microservice.md) | [Siguiente: Referencia completa de configuración](configuration.md)