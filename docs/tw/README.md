# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](../en/README.md) | [简体中文](../../README.md) | **繁體中文** | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

**[Retrofit](https://square.github.io/retrofit/) 支援將 HTTP API 化成 Java 介面，本元件將 Retrofit 和 Spring Boot 深度整合，並支援了多種實用功能增強。**

- **Spring Boot 3.x/4.x 專案**，請使用 retrofit-spring-boot-starter **4.x**
  - 由於 Spring Boot 4.x 預設使用 Jackson 3，而本元件預設 Converter 使用 Jackson 2，因此 **4.x 專案建議將全域 Converter 設定為 Jackson 3**
  - 設定方式：`retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x 專案**，請使用 retrofit-spring-boot-starter **2.x**，支援 Spring Boot 1.4.2 及以上版本

> 專案持續優化迭代，歡迎大家提 ISSUE 和 PR！能給一顆 star，是我們持續更新的最大支援！

GitHub：[https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee：[https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## 快速開始

### 引入依賴

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

引入依賴後即可使用。如遇問題，參見[常見問題](faq.md)。

### 定義 HTTP 介面

**介面必須使用 `@RetrofitClient` 註解標記！**

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

> 注意：**方法請求路徑慎用 `/` 開頭**。Retrofit 的路徑拼接規則：如果 `baseUrl = http://localhost:8080/api/test/`，方法路徑 `person` 的完整路徑為 `http://localhost:8080/api/test/person`；而方法路徑 `/person` 的完整路徑為 `http://localhost:8080/person`。

### 注入使用

將介面注入到其他 Service 中即可使用：

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

### HTTP 請求註解

HTTP 請求相關註解全部使用 Retrofit 原生註解：

| 註解分類 | 支援的註解 |
|----------|-----------|
| 請求方式 | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| 請求頭 | `@Header` `@HeaderMap` `@Headers` |
| Query 參數 | `@Query` `@QueryMap` `@QueryName` |
| Path 參數 | `@Path` |
| Form 參數 | `@Field` `@FieldMap` `@FormUrlEncoded` |
| 請求體 | `@Body` |
| 檔案上傳 | `@Multipart` `@Part` `@PartMap` |
| URL 參數 | `@Url` |

> 詳細資訊參考 [Retrofit 官方文件](https://square.github.io/retrofit/)

## 功能特性

- [x] [HTTP 回應結果自動適配](response-adaptation.md)
- [x] [自訂資料轉換器](converter.md)
- [x] [自訂 OkHttpClient與 Call.Factory SPI](okhttp-client.md)
- [x] [方法級超時設定](timeout.md)
- [x] [日誌列印](logging.md)
- [x] [請求重試](retry.md)
- [x] [攔截器](interceptor.md)
- [x] [熔斷降級](degrade.md)
- [x] [錯誤解碼器](error-decoder.md)
- [x] [微服務之間的 HTTP 呼叫](microservice.md)
- [x] [自訂 RetrofitClient 註解](custom-annotation.md)
- [x] [全量設定項參考](configuration.md)
- [x] [其他功能範例](examples.md)
- [x] [常見問題](faq.md)

## 回饋建議

如有任何問題，歡迎提 issue 或者加 QQ 群回饋。

群號：806714302

![QQ群图片](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)