# Конфигурация таймаутов на уровне метода
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | **Русский**

Компонент поддерживает настройку параметров таймаута на уровне метода или класса через аннотацию `@Timeout`, переопределяя глобальную конфигурацию таймаутов.

## Цепочка приоритетов

```
Метод @Timeout → Класс @Timeout → Глобальная конфигурация (GlobalTimeoutProperty)
```

- Значение по умолчанию свойств `@Timeout` равно `-1`, что означает "не настроено, наследуется от верхнего уровня цепочки приоритетов"
- Значение `0` означает "без таймаута"
- Положительное число означает конкретное значение таймаута в миллисекундах
- `-1` является недопустимым значением для таймаутов OkHttp (OkHttp принимает только 0 и положительные числа), поэтому используется как маркер "не настроено" и не конфликтует с допустимыми значениями таймаута

## Четыре параметра таймаута

| Свойство | Значение | Значение по умолчанию |
|------|------|--------|
| `connectTimeoutMs` | Таймаут подключения (миллисекунды) | `-1` (наследование от верхнего уровня) |
| `readTimeoutMs` | Таймаут чтения (миллисекунды) | `-1` (наследование от верхнего уровня) |
| `writeTimeoutMs` | Таймаут записи (миллисекунды) | `-1` (наследование от верхнего уровня) |
| `callTimeoutMs` | Таймаут полного вызова (миллисекунды) | `-1` (наследование от верхнего уровня) |

## @Timeout на уровне класса

Объявите `@Timeout` на интерфейсе, чтобы установить таймаут для всех методов этого интерфейса:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## @Timeout на уровне метода

Объявите `@Timeout` на определённом методе, чтобы переопределить таймаут на уровне класса или глобальную конфигурацию:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Наследует readTimeoutMs = 5000 на уровне класса
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Переопределение на уровне метода: для медленного запроса используется более длинный таймаут
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## @Timeout только на уровне метода (без аннотации на уровне класса)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Использует глобальную конфигурацию таймаутов
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Только для этого метода используется пользовательский таймаут
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

Предыдущий: [Пользовательский OkHttpClient](okhttp-client.md) | Следующий: [Логирование](logging.md)