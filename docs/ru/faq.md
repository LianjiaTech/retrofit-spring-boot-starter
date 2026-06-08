# Часто задаваемые вопросы
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | [한국어](../ko/faq.md) | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | **Русский**

## RetrofitAutoConfiguration не загружается автоматически

В некоторых сценариях (например, при использовании `@SpringBootApplication(exclude = ...)` или в проектах с смешанной XML-конфигурацией) `RetrofitAutoConfiguration` может не загружаться нормально. В этом случае можно вручную настроить импорт:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Если проект всё ещё использует конфигурационные файлы Spring XML, необходимо добавить класс автоматической конфигурации Spring Boot в XML-конфигурационный файл:

```xml
<!-- Импорт класса автоматической конфигурации Spring Boot -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Конфигурационный файл Spring Boot не работает

Если конфигурация в `application.yml` или `application.properties` не работает, можно вручную настроить Bean `RetrofitProperties`:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // Вручную изменить значения конфигурации retrofitProperties
    return retrofitProperties;
}
```

## Различие между именем свойства path-math и именем класса PathMatchInterceptor

`path-math` в свойстве конфигурации `auto-set-prototype-scope-for-path-math-interceptor` — это историческое название, а соответствующий класс интерцептора называется `PathMatchInterceptor` (с `match`). Это известное историческое различие в命名овании, которое не влияет на функциональность.

## Ручное указание пути сканирования RetrofitClient

По умолчанию компонент автоматически использует путь сканирования Spring Boot для регистрации `RetrofitClient`. Если нужно вручную указать путь сканирования, добавьте аннотацию `@RetrofitScan` на классе конфигурации.

## Изменение конфигурации сериализации Jackson

Если нужно изменить поведение сериализации/десериализации Jackson, просто переопределите конфигурацию Spring Bean `JacksonConverterFactory`. По умолчанию компонент использует `retrofit2.converter.jackson.JacksonConverterFactory`; после его регистрации как Bean пользовательская конфигурация Jackson `ObjectMapper` будет автоматически применена.

---

[Назад к списку функций](../../README.md)