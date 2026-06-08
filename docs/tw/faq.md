# 常见问题
[English](../en/faq.md) | [简体中文](../cn/faq.md) | **繁體中文** | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration 無法自動載入

某些場景下（如使用 `@SpringBootApplication(exclude = ...)` 或混合 XML 配置的專案），`RetrofitAutoConfiguration` 可能無法正常載入。此时可以手動配置導入：

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

如果專案仍使用 Spring XML 配置檔案，需要在 XML 配置檔案中加上 Spring Boot 自動配置類：

```xml
<!-- 導入 Spring Boot 自動配置類 -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 配置檔案無法生效

如果 `application.yml` 或 `application.properties` 中的配置無法生效，可以手動配置 `RetrofitProperties` Bean：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手動修改 retrofitProperties 各項配置值
    return retrofitProperties;
}
```

## 屬性名 path-math 与類名 PathMatchInterceptor 的差異

配置屬性 `auto-set-prototype-scope-for-path-math-interceptor` 中的 `path-math` 是歷史命名，而對應的拦截器類名為 `PathMatchInterceptor`（使用 `match`）。這是一個已知的歷史命名差異，不影響功能使用。

## 手動指定 RetrofitClient 掃描路徑

預設情況下，元件自動使用 Spring Boot 掃描路徑进行 `RetrofitClient` 注册。如果需要手動指定掃描路徑，可以在配置類上加上 `@RetrofitScan` 注解。

## 修改 Jackson 序列化配置

如果需要自定义 Jackson 的序列化/反序列化行為，直接覆蓋 `JacksonConverterFactory` 的 Spring Bean 配置即可。元件預設使用 `retrofit2.converter.jackson.JacksonConverterFactory`，將其注册為 Bean 後，自定义的 Jackson `ObjectMapper` 配置会被自動應用。

---

[返回功能特性目錄](../tw/README.md)