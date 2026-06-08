# Кастомные OkHttpClient и Call.Factory SPI
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | **Русский**

Данный компонент создает сконфигурированный `OkHttpClient` (со всеми интерцепторами, таймаутами, пулом соединений и т.д.) для каждого интерфейса `@RetrofitClient` и использует его как `Call.Factory` для Retrofit. Ниже описаны два способа кастомизации:

## Кастомный OkHttpClient

Для конфигурации таймаутов можно использовать файл конфигурации или аннотацию `@Timeout` (см. [Конфигурация таймаутов](timeout.md)). Но если нужна более гибкая и сложная конфигурация OkHttpClient, рекомендуется реализовать кастомный OkHttpClient.

### Реализация интерфейса SourceOkHttpClientRegistrar

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // Регистрация customOkHttpClient, таймаут установлен на 1 секунду
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
```

### Указание OkHttpClient для интерфейса

С помощью `@RetrofitClient.sourceOkHttpClient` указывается OkHttpClient, который должен использоваться данным интерфейсом:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Кастомный Call.Factory SPI

Если нужно кастомизировать на уровне создания Call (например, динамический callTimeout, настройка на уровне запроса), можно реализовать SPI через `CallFactoryConfigurer`.

> **Почему нужен SPI?** `callTimeout` в OkHttp - это общее время выполнения вызова, которое нельзя надежно переопределить в интерцепторе (OkHttp завершает планирование таймаута до выполнения цепочки интерцепторов). `CallFactoryConfigurer` вмешивается на уровне создания Call, используя `OkHttpClient.newBuilder()` для создания легковесного производного клиента (с общим connectionPool и dispatcher) для переопределения на уровне каждого запроса.

### Реализация интерфейса CallFactoryConfigurer

```java
@Component
public class DynamicCallTimeoutConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        return new Call.Factory() {
            @Override
            public Call newCall(Request request) {
                Invocation invocation = request.tag(Invocation.class);
                if (invocation != null) {
                    MyCallTimeout ann = invocation.method().getAnnotation(MyCallTimeout.class);
                    if (ann != null) {
                        // newBuilder() разделяет connectionPool/dispatcher/interceptors, отличается только callTimeout
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // Без переопределения -> используется @Timeout или глобальное значение по умолчанию
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Действие только для определенных интерфейсов

```java
@Component
public class SelectiveCallFactoryConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        if (retrofitInterface == SlowApiService.class) {
            return baseClient.newBuilder()
                    .callTimeout(30_000, TimeUnit.MILLISECONDS)
                    .build();
        }
        // Для других интерфейсов возвращаем baseClient, эквивалентно поведению по умолчанию
        return baseClient;
    }
}
```

> Если bean `CallFactoryConfigurer` не зарегистрирован, поведение компонента остается неизменным. Когда `CallFactoryConfigurer` возвращает не `OkHttpClient`, аннотация `@Timeout` на уровне метода не действует -- пользователь должен сам обработать таймауты в кастомной реализации.

---

[Предыдущая: Кастомные конвертеры данных](converter.md) | [Следующая: Конфигурация таймаутов на уровне метода](timeout.md)