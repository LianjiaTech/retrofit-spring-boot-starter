# Preguntas frecuentes
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | **Español** | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration no se puede cargar automáticamente

En algunos escenarios (como usar `@SpringBootApplication(exclude = ...)` o proyectos con configuración XML mixta), `RetrofitAutoConfiguration` puede no cargar correctamente. En este caso, se puede configurar manualmente la importación:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Si el proyecto aún usa archivos de configuración XML de Spring, es necesario agregar la clase de autoconfiguración de Spring Boot en el archivo de configuración XML:

```xml
<!-- Importar la clase de autoconfiguración de Spring Boot -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## El archivo de configuración de Spring Boot no funciona

Si la configuración en `application.yml` o `application.properties` no funciona, se puede configurar manualmente el bean `RetrofitProperties`:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // Modificar manualmente los valores de cada propiedad de retrofitProperties
    return retrofitProperties;
}
```

## Diferencia entre el nombre de propiedad path-math y el nombre de clase PathMatchInterceptor

La propiedad de configuración `auto-set-prototype-scope-for-path-math-interceptor` contiene `path-math` como nombre histórico, mientras que la clase de interceptor correspondiente se llama `PathMatchInterceptor` (usando `match`). Esta es una diferencia de命名 histórico conocida que no afecta la funcionalidad.

## Especificar manualmente la ruta de escaneo de RetrofitClient

Por defecto, el componente usa automáticamente la ruta de escaneo de Spring Boot para el registro de `RetrofitClient`. Si se necesita especificar manualmente la ruta de escaneo, se puede agregar la anotación `@RetrofitScan` en la clase de configuración.

## Modificar la configuración de serialización de Jackson

Si se necesita personalizar el comportamiento de serialización/deserialización de Jackson, simplemente sobrescribir la configuración del bean de Spring de `JacksonConverterFactory`. El componente usa por defecto `retrofit2.converter.jackson.JacksonConverterFactory`, y al registrarlo como bean, la configuración personalizada del `ObjectMapper` de Jackson se aplicará automáticamente.

---

[Volver al índice de funciones](../../README.md)