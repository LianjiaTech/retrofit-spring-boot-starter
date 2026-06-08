# retrofit-spring-boot-starter

[English](../en/README.md) | [简体中文](../../README.md) | **繁體中文** | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)

**[retrofit](https://square.github.io/retrofit/) 支援將 HTTP API 轉化成 JAVA 介面，本元件將 Retrofit 和 SpringBoot 深度整合，並支援了多種實用功能增強。**

- **Spring Boot 3.x/4.x 專案，請使用 retrofit-spring-boot-starter 4.x**
    - 由於 Spring Boot 4.x 預設使用 jackson3，但是本元件預設 converter 使用的是 jackson2，因此**對於 Spring Boot 4.x 專案建議將全域 converter 設定為 jackson3**
    - 配置方式 `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x 專案，請使用 [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)**，支援 Spring Boot 1.4.2 及以上版本。

## 快速開始

### 引入依賴

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

對於絕大部分 Spring Boot 專案，引入依賴即可使用。

### 定義 HTTP JAVA 介面

**介面必須使用 `@RetrofitClient` 注解標記！**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

   /**
    * 根據 id 查詢使用者姓名
    */
   @POST("getName")
   String getName(@Query("id") Long id);
}
```

> 注意：**方法請求路徑慎用 `/` 開頭**。對於 Retrofit 而言，如果 `baseUrl=http://localhost:8080/api/test/`，方法請求路徑如果是 `person`，則该方法完整的請求路徑是 `http://localhost:8080/api/test/person`。而方法請求路徑如果是 `/person`，則该方法完整的請求路徑是 `http://localhost:8080/person`。

### 注入使用

**將介面注入到其它 Service 中即可使用！**

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

## 功能特性

- [HTTP 响应结果自动适配](response-adaptation.md)
- [自定义数据转换器](converter.md)
- [自定义 OkHttpClient 与 Call.Factory SPI](okhttp-client.md)
- [方法级超时配置](timeout.md)
- [日志打印](logging.md)
- [请求重试](retry.md)
- [拦截器](interceptor.md)
- [熔断降级](degrade.md)
- [错误解码器](error-decoder.md)
- [指标监控（Micrometer）](metrics.md)
- [Actuator Endpoint](actuator.md)
- [GraalVM Native Image / AOT 支持](aot.md)
- [微服务之间的 HTTP 调用](microservice.md)
- [自定义 RetrofitClient 注解](custom-annotation.md)
- [全量配置项参考](configuration.md)
- [其他功能示例](examples.md)
- [常见问题](faq.md)