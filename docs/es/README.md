# retrofit-spring-boot-starter

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | **Español** | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)

**[retrofit](https://square.github.io/retrofit/) permite convertir APIs HTTP en interfaces Java. Este componente integra Retrofit con SpringBoot de forma profunda y soporta diversas enhancements funcionales practicas.**

- **Para proyectos Spring Boot 3.x/4.x, use retrofit-spring-boot-starter 4.x**
    - Spring Boot 4.x usa jackson3 por defecto, pero el converter predeterminado de este componente usa jackson2. **Para proyectos Spring Boot 4.x se recomienda configurar el converter global como jackson3**
    - Metodo de configuracion: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Para proyectos Spring Boot 1.x/2.x, use [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)**, que soporta Spring Boot 1.4.2 y versiones superiores.

## Inicio rapido

### Agregar dependencia

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

Para la mayoria de proyectos Spring Boot, agregar la dependencia es suficiente para usar el componente.

### Definir la interfaz HTTP Java

**La interfaz debe estar marcada con la anotacion `@RetrofitClient`!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

   /**
    * Buscar nombre de usuario por ID
    */
   @POST("getName")
   String getName(@Query("id") Long id);
}
```

> Nota: **Use `/` al inicio del path del metodo con precaucion**. Para Retrofit, si `baseUrl=http://localhost:8080/api/test/`, y el path del metodo es `person`, el path completo sera `http://localhost:8080/api/test/person`. Si el path del metodo es `/person`, el path completo sera `http://localhost:8080/person`.

### Inyectar y usar

**Inyecte la interfaz en otro Service para usarla!**

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
       // call userService
    }
}
```

## Caracteristicas funcionales

- [Adaptacion automatica de resultados de respuesta HTTP](response-adaptation.md)
- [Convertidor de datos personalizado](converter.md)
- [OkHttpClient y Call.Factory SPI personalizados](okhttp-client.md)
- [Configuracion de timeout a nivel de metodo](timeout.md)
- [Registro de logs](logging.md)
- [Reintento de solicitudes](retry.md)
- [Interceptores](interceptor.md)
- [Circuit Breaker / Degradacion](degrade.md)
- [Decodificador de errores](error-decoder.md)
- [Monitoreo de metricas (Micrometer)](metrics.md)
- [Actuator Endpoint](actuator.md)
- [Soporte GraalVM Native Image / AOT](aot.md)
- [Invocacion HTTP entre microservicios](microservice.md)
- [Anotacion RetrofitClient personalizada](custom-annotation.md)
- [Referencia completa de configuracion](configuration.md)
- [Otros ejemplos de funcionalidad](examples.md)
- [Preguntas frecuentes](faq.md)