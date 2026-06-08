# 常见问题
[English](../en/faq.md) | **简体中文** | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration 无法自动加载

某些场景下（如使用 `@SpringBootApplication(exclude = ...)` 或混合 XML 配置的项目），`RetrofitAutoConfiguration` 可能无法正常加载。此时可以手动配置导入：

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

如果项目仍使用 Spring XML 配置文件，需要在 XML 配置文件中加上 Spring Boot 自动配置类：

```xml
<!-- 导入 Spring Boot 自动配置类 -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 配置文件无法生效

如果 `application.yml` 或 `application.properties` 中的配置无法生效，可以手动配置 `RetrofitProperties` Bean：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手动修改 retrofitProperties 各项配置值
    return retrofitProperties;
}
```

## 属性名 path-math 与类名 PathMatchInterceptor 的差异

配置属性 `auto-set-prototype-scope-for-path-math-interceptor` 中的 `path-math` 是历史命名，而对应的拦截器类名为 `PathMatchInterceptor`（使用 `match`）。这是一个已知的历史命名差异，不影响功能使用。

## 手动指定 RetrofitClient 扫描路径

默认情况下，组件自动使用 Spring Boot 扫描路径进行 `RetrofitClient` 注册。如果需要手动指定扫描路径，可以在配置类上加上 `@RetrofitScan` 注解。

## 修改 Jackson 序列化配置

如果需要自定义 Jackson 的序列化/反序列化行为，直接覆盖 `JacksonConverterFactory` 的 Spring Bean 配置即可。组件默认使用 `retrofit2.converter.jackson.JacksonConverterFactory`，将其注册为 Bean 后，自定义的 Jackson `ObjectMapper` 配置会被自动应用。

---

[返回功能特性目录](../README.md)