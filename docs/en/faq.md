# FAQ
**English** | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration Cannot Be Auto-Loaded

In some scenarios (such as using `@SpringBootApplication(exclude = ...)` or projects with mixed XML configuration), `RetrofitAutoConfiguration` may not load properly. In this case, you can manually configure the import:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

If the project still uses Spring XML configuration files, you need to add the Spring Boot auto-configuration class in the XML configuration file:

```xml
<!-- Import Spring Boot auto-configuration class -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot Configuration File Not Taking Effect

If the configuration in `application.yml` or `application.properties` does not take effect, you can manually configure the `RetrofitProperties` Bean:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // Manually modify the configuration values of retrofitProperties
    return retrofitProperties;
}
```

## Difference Between Property Name path-math and Class Name PathMatchInterceptor

The `path-math` in the configuration property `auto-set-prototype-scope-for-path-math-interceptor` is a historical naming convention, while the corresponding interceptor class name is `PathMatchInterceptor` (using `match`). This is a known historical naming difference and does not affect functionality.

## Manually Specifying RetrofitClient Scan Path

By default, the component automatically uses the Spring Boot scan path for `RetrofitClient` registration. If you need to manually specify the scan path, you can add the `@RetrofitScan` annotation on the configuration class.

## Modifying Jackson Serialization Configuration

If you need to customize Jackson's serialization/deserialization behavior, simply override the `JacksonConverterFactory` Spring Bean configuration. The component uses `retrofit2.converter.jackson.JacksonConverterFactory` by default; once it is registered as a Bean, your custom Jackson `ObjectMapper` configuration will be automatically applied.

---

[Back to Feature Index](../../README.md)