# Preguntas frecuentes
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | **Español** | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration no se puede cargar automaticamente

En algunos escenarios (como usar `@SpringBootApplication(exclude = ...)` o proyectos con configuracion XML mixta), `RetrofitAutoConfiguration` puede no cargarse normalmente. En este caso se puede configurar manualmente la importacion:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Si el proyecto todavia usa archivos de configuracion Spring XML, se debe agregar la clase de autoconfiguracion de Spring Boot en el archivo de configuracion XML:

```xml
<!-- Importar clase de autoconfiguracion de Spring Boot -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Archivo de configuracion de Spring Boot no surte efecto

Si la configuracion en `application.yml` o `application.properties` no surte efecto, se puede configurar manualmente el bean `RetrofitProperties`:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // Modificar manualmente los valores de configuracion de retrofitProperties
    return retrofitProperties;
}
```

## Diferencia entre el nombre de propiedad path-math y el nombre de clase PathMatchInterceptor

La propiedad de configuracion `auto-set-prototype-scope-for-path-math-interceptor` contiene `path-math`, que es un nombre historico, mientras que la clase de interceptor correspondiente se llama `PathMatchInterceptor` (usando `match`). Esta es una diferencia de nomenclatura historica conocida que no afecta la funcionalidad.

## Especificar manualmente la ruta de escaneo de RetrofitClient

Por defecto, el componente usa automaticamente la ruta de escaneo de Spring Boot para el registro de `RetrofitClient`. Si se necesita especificar manualmente la ruta de escaneo, se puede agregar la anotacion `@RetrofitScan` en la clase de configuracion.

## Modificar la configuracion de serializacion de Jackson

Si se necesita personalizar el comportamiento de serializacion/deserializacion de Jackson, basta con sobrescribir la configuracion del Spring Bean de `JacksonConverterFactory`. El componente usa por defecto `retrofit2.converter.jackson.JacksonConverterFactory`, al registrarlo como Bean, la configuracion personalizada de `ObjectMapper` de Jackson se aplicara automaticamente.

---

[Índice de características](../../README.md)