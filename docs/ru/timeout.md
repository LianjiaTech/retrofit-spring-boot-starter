# Конфигурация таймаутов на уровне метода
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | **Русский**

Компонент поддерживает настройку параметров таймаута на уровне метода или класса с помощью аннотации `@Timeout`, переопределяя глобальную конфигурацию таймаутов.

## Цепочка приоритета

```
Метод @Timeout -> Класс @Timeout -> Глобальная конфигурация (GlobalTimeoutProperty)
```

- Значение атрибута `@Timeout` по умолчанию `-1` означает "не настроено, наследуется от верхнего уровня цепочки приоритета"
- Значение `0` означает "нет таймаута"
- Положительное число означает конкретное значение таймаута в миллисекундах
- `-1` является недопустимым значением для таймаутов OkHttp (OkHttp принимает только 0 и положительные числа), используется как маркер "не настроено", что не конфликтует с допустимыми значениями таймаута

## Четыре измерения таймаута

| Атрибут | Значение | Значение по умолчанию |
|---------|----------|----------------------|
| `connectTimeoutMs` | Таймаут соединения (мс) | `-1` (наследуется от верхнего уровня) |
| `readTimeoutMs` | Таймаут чтения (мс) | `-1` (наследуется от верхнего уровня) |
| `writeTimeoutMs` | Таймаут записи (мс) | `-1` (наследуется от верхнего уровня) |
| `callTimeoutMs` | Таймаут полного вызова (мс) | `-1` (наследуется от верхнего уровня) |

## @Timeout на уровне класса

Укажите `@Timeout` на интерфейсе, чтобы задать таймаут для всех методов этого интерфейса:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## @Timeout на уровне метода

Укажите `@Timeout` на конкретном методе, чтобы переопределить таймаут на уровне класса или глобальный таймаут:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Наследует readTimeoutMs = 5000 от уровня класса
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Переопределение на уровне метода: для медленного запроса используется больший таймаут
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## @Timeout только на уровне метода (без аннотации на уровне класса)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Использует глобальную конфигурацию таймаута
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Только этот метод использует кастомный таймаут
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Механизм реализации

- `@Timeout` на уровне класса: обрабатывается при создании OkHttpClient, нулевые накладные расходы при выполнении
- `@Timeout` на уровне метода: `TimeoutCallFactory` предварительно создает clone OkHttpClient для каждого метода, при выполнении ищется через Invocation tag; для интерфейсов без аннотаций нулевые дополнительные накладные расходы

---

[Предыдущая: Кастомные OkHttpClient](okhttp-client.md) | [Следующая: Логирование](logging.md)