# Часто задаваемые вопросы
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | **Русский**

## RetrofitAutoConfiguration не может автоматически загрузиться

В некоторых сценариях (например, при использовании `@SpringBootApplication(exclude = ...)` или проектах с смешанной XML-конфигурацией) `RetrofitAutoConfiguration` может не загрузиться正常но. В этом случае можно手动но импортировать конфигурацию:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Если проект все еще использует XML-конфигурацию Spring, нужно добавить класс авто-конфигурации Spring Boot в XML-конфигурационный файл:

```xml
<!-- Импорт класса авто-конфигурации Spring Boot -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Конфигурация Spring Boot не вступает в силу

Если конфигурация в `application.yml` или `application.properties` не вступает в силу, можно手动но конфигурировать `RetrofitProperties` Bean:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手动修改 retrofitProperties 各项配置值
    return retrofitProperties;
}
```

## Различие между свойством path-math и именем класса PathMatchInterceptor

`path-math` в свойстве конфигурации `auto-set-prototype-scope-for-path-math-interceptor` является историческим названием, в то время как соответствующий класс interceptor называется `PathMatchInterceptor` (с `match`). Это известное историческое различие в命名ании, которое не влияет на функциональность.

## Ручное указание пути сканирования RetrofitClient

По умолчанию компонент автоматически использует путь сканирования Spring Boot для регистрации `RetrofitClient`. Если нужно手动но указать путь сканирования, можно добавить аннотацию `@RetrofitScan` на классе конфигурации.

## Изменение конфигурации сериализации Jackson

Если нужно кастомизировать поведение сериализации/десериализации Jackson, просто переопределите конфигурацию Spring Bean `JacksonConverterFactory`. Компонент по умолчанию использует `retrofit2.converter.jackson.JacksonConverterFactory`; после регистрации его как Bean ваша кастомная конфигурация Jackson `ObjectMapper` будет автоматически применена.

---

[Индекс функций](../../README.md)