# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | **Español** | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

**[Retrofit](https://square.github.io/retrofit/) permite declarar APIs HTTP como interfaces Java. Este componente integra profundamente Retrofit con Spring Boot y proporciona diversas funcionalidades prácticas de mejora.**

- **Proyectos Spring Boot 3.x/4.x**, utilice retrofit-spring-boot-starter **4.x**
  - Como Spring Boot 4.x utiliza Jackson 3 por defecto mientras este componente usa Jackson 2 como Converter predeterminado, se **recomienda configurar el Converter global como Jackson 3 para proyectos 4.x**
  - Configuración: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Proyectos Spring Boot 1.x/2.x**, utilice retrofit-spring-boot-starter **2.x**, compatible con Spring Boot 1.4.2 y superior

> El proyecto se optimiza e itera continuamente. ¡Envíe issues y PRs! Darle una star es el mayor apoyo para nuestras actualizaciones continuas!

GitHub: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Inicio rápido

### Agregar dependencia

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

Una vez agregada la dependencia, puede comenzar a usarlo. Si encuentra algún problema, consulte las [Preguntas frecuentes](faq.md).

### Definir interfaz HTTP

**La interfaz debe estar anotada con `@RetrofitClient`!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * Consultar nombre de usuario por id
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Nota: **Evite comenzar las rutas de petición de métodos con `/`**. Las reglas de concatenación de paths de Retrofit: si `baseUrl = http://localhost:8080/api/test/`, la ruta de método `person` resulta en la ruta completa `http://localhost:8080/api/test/person`; mientras que la ruta de método `/person` resulta en la ruta completa `http://localhost:8080/person`.

### Inyectar y usar

Inyecte la interfaz en otros servicios para usarla:

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // llamar userService
    }
}
```

### Anotaciones de petición HTTP

Las anotaciones relacionadas con peticiones HTTP utilizan las anotaciones nativas de Retrofit:

| Categoría de anotación | Anotaciones soportadas |
|------------------------|------------------------|
| Método de petición | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| Headers de petición | `@Header` `@HeaderMap` `@Headers` |
| Parámetros Query | `@Query` `@QueryMap` `@QueryName` |
| Parámetros Path | `@Path` |
| Parámetros Form | `@Field` `@FieldMap` `@FormUrlEncoded` |
| Body de petición | `@Body` |
| Subida de archivos | `@Multipart` `@Part` `@PartMap` |
| Parámetro URL | `@Url` |

> Para información detallada, consulte la [documentación oficial de Retrofit](https://square.github.io/retrofit/)

## Índice de funcionalidades

- [x] [Adaptación automática de respuestas HTTP](response-adaptation.md)
- [x] [Convertidor de datos personalizado](converter.md)
- [x] [OkHttpClient personalizado & Call.Factory SPI](okhttp-client.md)
- [x] [Configuración de timeout a nivel de método](timeout.md)
- [x] [Log de peticiones](logging.md)
- [x] [Reintento de peticiones](retry.md)
- [x] [Interceptores](interceptor.md)
- [x] [Circuit breaker / Degradación](degrade.md)
- [x] [Decodificador de errores](error-decoder.md)
- [x] [Peticiones HTTP entre microservicios](microservice.md)
- [x] [Anotación RetrofitClient personalizada](custom-annotation.md)
- [x] [Referencia completa de configuración](configuration.md)
- [x] [Otros ejemplos de funcionalidades](examples.md)
- [x] [Preguntas frecuentes](faq.md)

## Feedback / Sugerencias

Si tiene alguna pregunta, no dude en enviar un issue o unirse al grupo QQ para feedback.

Grupo QQ: 806714302

![Grupo QQ](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)