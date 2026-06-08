# Anotacion RetrofitClient personalizada
[English](../en/custom-annotation.md) | [简体中文](../cn/custom-annotation.md) | [繁體中文](../tw/custom-annotation.md) | [日本語](../ja/custom-annotation.md) | [한국어](../ko/custom-annotation.md) | **Español** | [Türkçe](../tr/custom-annotation.md) | [Русский](../ru/custom-annotation.md)

A veces, los valores por defecto de las anotaciones `@RetrofitClient`, `@Retry`, `@Logging`, `@Resilience4jDegrade`, etc. en la interfaz Java no satisfacen las necesidades del negocio. Una opcion es modificar los atributos de anotacion correspondientes en cada interfaz, pero esto lleva a que muchas interfaces deban realizar la misma logica, lo cual no es elegante. Otra opcion es crear una anotacion RetrofitClient personalizada, y luego otras interfaces solo necesitan usar la anotacion personalizada.

A continuacion se define la anotacion personalizada `@MyRetrofitClient`, que combina los atributos por defecto de multiples anotaciones en una sola anotacion:

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

[Anterior: Invocacion HTTP entre microservicios](microservice.md) | [Siguiente: Referencia completa de configuracion](configuration.md)