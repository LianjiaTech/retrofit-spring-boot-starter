# 常見問題
[English](../en/faq.md) | [简体中文](../cn/faq.md) | **繁體中文** | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration 無法自動載入

某些場景下（如使用 `@SpringBootApplication(exclude = ...)` 或混合 XML 設定的專案），`RetrofitAutoConfiguration` 可能無法正常載入。此時可以手動設定匯入：

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

如果專案仍使用 Spring XML 設定檔案，需要在 XML 設定檔案中加上 Spring Boot 自動設定類：

```xml
<!-- 匯入 Spring Boot 自動設定類 -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 設定檔案無法生效

如果 `application.yml` 或 `application.properties` 中的設定無法生效，可以手動設定 `RetrofitProperties` Bean：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手動修改 retrofitProperties 各項設定值
    return retrofitProperties;
}
```

## 屬性名 path-math 與類名 PathMatchInterceptor 的差異

設定屬性 `auto-set-prototype-scope-for-path-math-interceptor` 中的 `path-math` 是歷史命名，而對應的攔截器類名為 `PathMatchInterceptor`（使用 `match`）。這是一個已知的歷史命名差異，不影響功能使用。

## 手動指定 RetrofitClient 掃描路徑

預設情況下，元件自動使用 Spring Boot 掃描路徑進行 `RetrofitClient` 註冊。如果需要手動指定掃描路徑，可以在設定類上加上 `@RetrofitScan` 註解。

## 修改 Jackson 序列化設定

如果需要自訂 Jackson 的序列化/反序列化行為，直接覆寫 `JacksonConverterFactory` 的 Spring Bean 設定即可。元件預設使用 `retrofit2.converter.jackson.JacksonConverterFactory`，將其註冊為 Bean 後，自訂的 Jackson `ObjectMapper` 設定會被自動應用。

---

[返回功能特性目錄](../../README.md)