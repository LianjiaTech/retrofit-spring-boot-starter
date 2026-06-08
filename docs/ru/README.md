# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | **Русский**

**[Retrofit](https://square.github.io/retrofit/) позволяет описывать HTTP API как Java-интерфейсы. Этот компонент глубоко интегрирует Retrofit с Spring Boot и поддерживает множество полезных функциональных расширений.**

- **Проекты Spring Boot 3.x/4.x**, используйте retrofit-spring-boot-starter **4.x**
  - Поскольку Spring Boot 4.x по умолчанию использует Jackson 3, а данный компонент по умолчанию использует Jackson 2 как Converter, **для проектов 4.x рекомендуется установить глобальный Converter на Jackson 3**
  - Конфигурация: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Проекты Spring Boot 1.x/2.x**, используйте retrofit-spring-boot-starter **2.x**, поддерживает Spring Boot 1.4.2 и выше

> Проект постоянно оптимизируется и развивается. Приветствуются ISSUE и PR! Поставить star — лучшая поддержка наших дальнейших обновлений!

GitHub: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## Быстрый старт

### Добавление зависимости

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

После добавления зависимости можно сразу использовать. При возникновении проблем обратитесь к [Часто задаваемые вопросы](faq.md).

### Определение HTTP-интерфейса

**Интерфейс должен быть помечен аннотацией `@RetrofitClient`!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * Запрос имени пользователя по id
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> Внимание: **Избегайте использования `/` в начале пути метода запроса**. Правила конкатенации путей Retrofit: если `baseUrl = http://localhost:8080/api/test/`, методный путь `person` даст полный путь `http://localhost:8080/api/test/person`; а методный путь `/person` даст полный путь `http://localhost:8080/person`.

### Внедрение и использование

Внедрите интерфейс в другие сервисы для использования:

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // вызов userService
    }
}
```

### Аннотации HTTP-запросов

Все аннотации, связанные с HTTP-запросами, используют стандартные аннотации Retrofit:

| Категория аннотаций | Поддерживаемые аннотации |
|---------------------|--------------------------|
| Метод запроса | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| Заголовки запроса | `@Header` `@HeaderMap` `@Headers` |
| Параметры Query | `@Query` `@QueryMap` `@QueryName` |
| Параметры Path | `@Path` |
| Параметры Form | `@Field` `@FieldMap` `@FormUrlEncoded` |
| Тело запроса | `@Body` |
| Загрузка файлов | `@Multipart` `@Part` `@PartMap` |
| Параметр URL | `@Url` |

> Подробнее см. [официальную документацию Retrofit](https://square.github.io/retrofit/)

## Функциональные возможности

- [x] [Автоматическая адаптация HTTP-ответов](response-adaptation.md)
- [x] [Пользовательский преобразователь данных](converter.md)
- [x] [Пользовательский OkHttpClient и Call.Factory SPI](okhttp-client.md)
- [x] [Конфигурация таймаута на уровне метода](timeout.md)
- [x] [Логирование запросов](logging.md)
- [x] [Повторная попытка запроса](retry.md)
- [x] [Интерцепторы](interceptor.md)
- [x] [Обрыв цепи и деградация](degrade.md)
- [x] [Декодер ошибок](error-decoder.md)
- [x] [HTTP-вызовы между микросервисами](microservice.md)
- [x] [Пользовательская аннотация RetrofitClient](custom-annotation.md)
- [x] [Полная справка по конфигурации](configuration.md)
- [x] [Примеры других функций](examples.md)
- [x] [Часто задаваемые вопросы](faq.md)

## Обратная связь

Если у вас есть вопросы, вы можете создать issue или присоединиться к группе QQ для обратной связи.

Группа QQ: 806714302

![Группа QQ](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)