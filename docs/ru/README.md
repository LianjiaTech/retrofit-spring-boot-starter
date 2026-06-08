# retrofit-spring-boot-starter

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | **Русский**

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[Retrofit](https://square.github.io/retrofit/) позволяет преобразовать HTTP API в Java-интерфейсы. Этот компонент глубоко интегрирует Retrofit с Spring Boot и поддерживает различные практичные расширения функциональности.

- **Для проектов Spring Boot 3.x/4.x используйте retrofit-spring-boot-starter 4.x**
  - Поскольку Spring Boot 4.x по умолчанию использует Jackson 3, а данный компонент использует Jackson 2 в качестве конвертера по умолчанию, **для проектов на Spring Boot 4.x рекомендуется установить глобальный конвертер на Jackson 3**
  - Способ настройки: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Для проектов Spring Boot 1.x/2.x используйте [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)**, который поддерживает Spring Boot 1.4.2 и выше

GitHub: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Быстрый старт

### Добавление зависимости

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

Для большинства проектов Spring Boot достаточно добавить зависимость. Если компонент не работает после внедрения зависимости, попробуйте следующие решения:

#### Импорт авто-конфигурации вручную

В некоторых случаях `RetrofitAutoConfiguration` может не загрузиться автоматически. В этом случае импортируйте конфигурацию вручную:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

Если проект использует XML-конфигурацию Spring, добавьте класс авто-конфигурации Spring Boot в XML-файл:

```xml
<!-- Импорт класса авто-конфигурации Spring Boot -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

### Определение HTTP-интерфейса

**Интерфейсы должны быть аннотированы `@RetrofitClient`!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * Получить имя пользователя по ID
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Примечание: **Не используйте `/` в начале пути запроса метода**. Для Retrofit, если `baseUrl = http://localhost:8080/api/test/`:
> - Путь метода `person` даст полный URL: `http://localhost:8080/api/test/person`.
> - Путь метода `/person` даст полный URL: `http://localhost:8080/person`.

### Внедрение и использование

**Внедрите интерфейс в другие Service-компоненты!**

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // Вызов методов userService
    }
}
```

**По умолчанию интерфейсы `RetrofitClient` автоматически регистрируются через путь сканирования компонентов Spring Boot**. Также можно указать кастомный путь сканирования с помощью `@RetrofitScan` на классе конфигурации.

### Аннотации HTTP-запросов

Аннотации, связанные с HTTP-запросами, используют原生ные аннотации Retrofit:

| Категория аннотаций | Поддерживаемые аннотации |
|---------------------|--------------------------|
| Методы запроса      | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| Заголовки запроса   | `@Header` `@HeaderMap` `@Headers` |
| Query-параметры     | `@Query` `@QueryMap` `@QueryName` |
| Path-параметры      | `@Path` |
| Form-параметры      | `@Field` `@FieldMap` `@FormUrlEncoded` |
| Тело запроса        | `@Body` |
| Загрузка файлов     | `@Multipart` `@Part` `@PartMap` |
| URL-параметры       | `@Url` |

> Подробнее см. официальную документацию: [Retrofit Official Documentation](https://square.github.io/retrofit/)

## Возможности

- [x] [Автоматическая адаптация HTTP-ответов](response-adaptation.md)
- [x] [Кастомные конвертеры данных](converter.md)
- [x] [Кастомные OkHttpClient и Call.Factory SPI](okhttp-client.md)
- [x] [Конфигурация таймаутов на уровне метода](timeout.md)
- [x] [Логирование](logging.md)
- [x] [Повторные попытки запросов](retry.md)
- [x] [Интерцепторы](interceptor.md)
- [x] [Circuit Breaker (предохранитель)](degrade.md)
- [x] [Декодер ошибок](error-decoder.md)
- [x] [Метрики (Micrometer)](metrics.md)
- [x] [Actuator Endpoint](actuator.md)
- [x] [GraalVM Native Image / AOT](aot.md)
- [x] [HTTP-вызовы между микросервисами](microservice.md)
- [x] [Кастомные аннотации RetrofitClient](custom-annotation.md)
- [x] [Справочник конфигурации](configuration.md)
- [x] [Примеры использования](examples.md)
- [x] [Часто задаваемые вопросы](faq.md)