# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

**English** | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

**[Retrofit](https://square.github.io/retrofit/) enables declaring HTTP APIs as Java interfaces. This component deeply integrates Retrofit with Spring Boot and provides a variety of practical feature enhancements.**

- **Spring Boot 3.x/4.x projects**, please use retrofit-spring-boot-starter **4.x**
  - Since Spring Boot 4.x defaults to Jackson 3 while this component uses Jackson 2 as the default Converter, it is **recommended to set the global Converter to Jackson 3 for 4.x projects**
  - Configuration: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x projects**, please use retrofit-spring-boot-starter **2.x**, supporting Spring Boot 1.4.2 and above

> The project is continuously optimized and iterated. Feel free to submit issues and PRs! Giving us a star is the greatest support for our continued updates!

GitHub: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Quick Start

### Add Dependency

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

Once the dependency is added, you can start using it. If you encounter any issues, see the [FAQ](faq.md).

### Define HTTP Interface

**The interface must be annotated with `@RetrofitClient`!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * Query user name by id
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Note: **Avoid starting method request paths with `/`**. Retrofit's path concatenation rules: if `baseUrl = http://localhost:8080/api/test/`, the method path `person` results in the full path `http://localhost:8080/api/test/person`; whereas the method path `/person` results in the full path `http://localhost:8080/person`.

### Inject and Use

Inject the interface into other services to use it:

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

### HTTP Request Annotations

HTTP request-related annotations all use Retrofit's native annotations:

| Annotation Category | Supported Annotations |
|---------------------|----------------------|
| Request Method | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| Request Headers | `@Header` `@HeaderMap` `@Headers` |
| Query Parameters | `@Query` `@QueryMap` `@QueryName` |
| Path Parameters | `@Path` |
| Form Parameters | `@Field` `@FieldMap` `@FormUrlEncoded` |
| Request Body | `@Body` |
| File Upload | `@Multipart` `@Part` `@PartMap` |
| URL Parameter | `@Url` |

> For detailed information, refer to the [Retrofit official documentation](https://square.github.io/retrofit/)

## Feature Index

- [x] [HTTP Response Adaptation](response-adaptation.md)
- [x] [Custom Data Converter](converter.md)
- [x] [Custom OkHttpClient & Call.Factory SPI](okhttp-client.md)
- [x] [Method-Level Timeout Configuration](timeout.md)
- [x] [Request Logging](logging.md)
- [x] [Request Retry](retry.md)
- [x] [Interceptors](interceptor.md)
- [x] [Circuit Breaker & Fallback](degrade.md)
- [x] [Error Decoder](error-decoder.md)
- [x] [HTTP Calls Between Microservices](microservice.md)
- [x] [Custom RetrofitClient Annotation](custom-annotation.md)
- [x] [Full Configuration Reference](configuration.md)
- [x] [Other Feature Examples](examples.md)
- [x] [FAQ](faq.md)

## Feedback

If you have any questions, feel free to submit an issue or join the QQ group for feedback.

QQ Group: 806714302

![QQ Group](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)