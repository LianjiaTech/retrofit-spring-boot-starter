# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](docs/en/README.md) | **简体中文** | [繁體中文](docs/tw/README.md) | [日本語](docs/ja/README.md) | [한국어](docs/ko/README.md) | [Español](docs/es/README.md) | [Türkçe](docs/tr/README.md) | [Русский](docs/ru/README.md)

**[Retrofit](https://square.github.io/retrofit/) 支持将 HTTP API 化成 Java 接口，本组件将 Retrofit 和 Spring Boot 深度整合，并支持了多种实用功能增强。**

- **Spring Boot 3.x/4.x 项目**，请使用 retrofit-spring-boot-starter **4.x**
  - 由于 Spring Boot 4.x 默认使用 Jackson 3，而本组件默认 Converter 使用 Jackson 2，因此 **4.x 项目建议将全局 Converter 设置为 Jackson 3**
  - 配置方式：`retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x 项目**，请使用 [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)，支持 Spring Boot 1.4.2 及以上版本

> 项目持续优化迭代，欢迎大家提 ISSUE 和 PR！能给一颗 star，是对我们持续更新的最大支持！

GitHub：[https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee：[https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

引入依赖后即可使用。如遇问题，参见[常见问题](docs/cn/faq.md)。

### 定义 HTTP 接口

**接口必须使用 `@RetrofitClient` 注解标记！**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> 注意：**方法请求路径慎用 `/` 开头**。Retrofit 的路径拼接规则：如果 `baseUrl = http://localhost:8080/api/test/`，方法路径 `person` 的完整路径为 `http://localhost:8080/api/test/person`；而方法路径 `/person` 的完整路径为 `http://localhost:8080/person`。

### 注入使用

将接口注入到其他 Service 中即可使用：

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // 调用 userService
    }
}
```

默认情况下，自动使用 SpringBoot 扫描路径进行 `RetrofitClient` 注册，也可以在配置类加上 `@RetrofitScan` 手动指定扫描路径。

### HTTP 请求注解

HTTP 请求相关注解全部使用 Retrofit 原生注解：

| 注解分类 | 支持的注解 |
|----------|-----------|
| 请求方式 | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| 请求头 | `@Header` `@HeaderMap` `@Headers` |
| Query 参数 | `@Query` `@QueryMap` `@QueryName` |
| Path 参数 | `@Path` |
| Form 参数 | `@Field` `@FieldMap` `@FormUrlEncoded` |
| 请求体 | `@Body` |
| 文件上传 | `@Multipart` `@Part` `@PartMap` |
| URL 参数 | `@Url` |

> 详细信息参考 [Retrofit 官方文档](https://square.github.io/retrofit/)

## 功能特性

- [x] [HTTP 响应结果自动适配](docs/cn/response-adaptation.md)
- [x] [自定义数据转换器](docs/cn/converter.md)
- [x] [自定义 OkHttpClient 与 Call.Factory SPI](docs/cn/okhttp-client.md)
- [x] [方法级超时配置](docs/cn/timeout.md)
- [x] [日志打印](docs/cn/logging.md)
- [x] [请求重试](docs/cn/retry.md)
- [x] [拦截器](docs/cn/interceptor.md)
- [x] [熔断降级](docs/cn/degrade.md)
- [x] [错误解码器](docs/cn/error-decoder.md)
- [x] [指标监控（Micrometer）](docs/cn/metrics.md)
- [x] [Actuator Endpoint](docs/cn/actuator.md)
- [x] [GraalVM Native Image / AOT 支持](docs/cn/aot.md)
- [x] [微服务之间的 HTTP 调用](docs/cn/microservice.md)
- [x] [自定义 RetrofitClient 注解](docs/cn/custom-annotation.md)
- [x] [全量配置项参考](docs/cn/configuration.md)
- [x] [其他功能示例](docs/cn/examples.md)
- [x] [常见问题](docs/cn/faq.md)

## 反馈建议

如有任何问题，欢迎提 issue 或者加 QQ 群反馈。

群号：806714302

![QQ群图片](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)