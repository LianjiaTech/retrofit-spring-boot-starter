# retrofit-spring-boot-starter

**English** | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[retrofit](https://square.github.io/retrofit/) enables the conversion of HTTP APIs into Java interfaces. This component deeply integrates Retrofit with Spring Boot and supports various practical feature enhancements.

- **For Spring Boot 3.x/4.x projects, use retrofit-spring-boot-starter 4.x**
  - Since Spring Boot 4.x uses Jackson3 by default, and this component also uses Jackson2 as its default converter, **it is recommended to set the global converter to Jackson3 for Spring Boot 4.x projects.**
  - Configuration method: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **For Spring Boot 1.x/2.x projects, use [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)**, which supports Spring Boot 1.4.2 and above.

GitHub project link: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee project link: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Quick Start

### Add Dependency

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

For most Spring Boot projects, adding the dependency is sufficient. If the component fails to work after dependency injection, try the following solutions:

#### Manual Auto-configuration Import

In some cases, `RetrofitAutoConfiguration` may not load properly. Attempt manual configuration import with the following code:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

If the project still uses Spring XML configuration files, add the Spring Boot auto-configuration class to the XML file:

```xml
<!-- Import Spring Boot auto-configuration class -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

### Define HTTP Java Interface

**Interfaces must be annotated with `@RetrofitClient`!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * Query username by ID
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Note: **Avoid using leading slashes (`/`) in method request paths**. For Retrofit, if `baseUrl = http://localhost:8080/api/test/`:
> - A method path `person` results in the full URL: `http://localhost:8080/api/test/person`.
> - A method path `/person` results in the full URL: `http://localhost:8080/person`.

### Injection and Usage

**Inject the interface into other Services for use!**

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // Call userService methods
    }
}
```

**By default, `RetrofitClient` interfaces are automatically registered via Spring Boot's component scanning path**. Alternatively, specify a custom scan path using `@RetrofitScan` on a configuration class.

## HTTP Request Annotations

HTTP request-related annotations use Retrofit's native annotations. A brief overview is provided below:

| Annotation Category | Supported Annotations |
|---------------------|-----------------------|
| Request Methods     | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| Request Headers     | `@Header` `@HeaderMap` `@Headers` |
| Query Parameters    | `@Query` `@QueryMap` `@QueryName` |
| Path Parameters     | `@Path` |
| Form-Encoded Params | `@Field` `@FieldMap` `@FormUrlEncoded` |
| Request Body        | `@Body` |
| File Upload         | `@Multipart` `@Part` `@PartMap` |
| URL Parameters      | `@Url` |

> For details, refer to the official documentation: [Retrofit Official Documentation](https://square.github.io/retrofit/)

## Feature Highlights

- [x] [HTTP Response Adaptation](response-adaptation.md)
- [x] [Custom Data Converters](converter.md)
- [x] [Custom OkHttpClient & Call.Factory SPI](okhttp-client.md)
- [x] [Method-level Timeout Configuration](timeout.md)
- [x] [Logging](logging.md)
- [x] [Request Retries](retry.md)
- [x] [Interceptors](interceptor.md)
- [x] [Circuit Breaking](degrade.md)
- [x] [Error Decoder](error-decoder.md)
- [x] [Metrics Monitoring (Micrometer)](metrics.md)
- [x] [Actuator Endpoint](actuator.md)
- [x] [GraalVM Native Image / AOT Support](aot.md)
- [x] [HTTP Calls Between Microservices](microservice.md)
- [x] [Custom RetrofitClient Annotations](custom-annotation.md)
- [x] [Configuration Properties](configuration.md)
- [x] [Additional Examples](examples.md)
- [x] [FAQ](faq.md)