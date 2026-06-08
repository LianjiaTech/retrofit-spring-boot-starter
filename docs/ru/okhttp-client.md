# Пользовательский OkHttpClient и Call.Factory
[English](../en/okhttp-client.md) | [简体中文](../cn/okhttp-client.md) | [繁體中文](../tw/okhttp-client.md) | [日本語](../ja/okhttp-client.md) | [한국어](../ko/okhttp-client.md) | [Español](../es/okhttp-client.md) | [Türkçe](../tr/okhttp-client.md) | **Русский**

Компонент создаёт настроенный `OkHttpClient` (содержащий все интерцепторы, таймауты, пулы подключений и т.д.) для каждого интерфейса `@RetrofitClient` и использует его как `Call.Factory` Retrofit. Ниже описаны два способа настройки:

## Пользовательский OkHttpClient

Для конфигурации таймаутов можно использовать конфигурационный файл или аннотацию `@Timeout` (см. [Конфигурация таймаутов](timeout.md)). Однако если требуется более гибкая и сложная конфигурация OkHttpClient, рекомендуется реализовать пользовательский OkHttpClient.

### Реализация интерфейса SourceOkHttpClientRegistrar

```java
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // Регистрация customOkHttpClient с таймаутом 1 секунда
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

Укажите OkHttpClient, который должен использоваться текущим интерфейсом, через `@RetrofitClient.sourceOkHttpClient`:

```java
@RetrofitClient(baseUrl = "${test.baseUrl}", sourceOkHttpClient = "customOkHttpClient")
public interface CustomOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## SPI для пользовательского Call.Factory

Если требуется кастомизация на уровне создания Call (например, динамический callTimeout, настройка на уровне запроса), это можно реализовать через SPI `CallFactoryConfigurer`.

> **Зачем нужен SPI?** `callTimeout` OkHttp — это общее время выполнения вызова, которое нельзя надежно переопределить в интерцепторе (OkHttp завершает планирование таймаута до выполнения цепочки интерцепторов). `CallFactoryConfigurer` вмешивается на уровне создания Call, используя `OkHttpClient.newBuilder()` для создания lightweight client (разделяющего connectionPool и dispatcher), что позволяет переопределить параметры на уровне каждого запроса.

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
                        // newBuilder() разделяет connectionPool/dispatcher/interceptors, только callTimeout отличается
                        return baseClient.newBuilder()
                                .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                                .build()
                                .newCall(request);
                    }
                }
                // Нет переопределения → используется @Timeout или глобальное значение по умолчанию
                return baseClient.newCall(request);
            }
        };
    }
}
```

### Применение только к определённым интерфейсам

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
        // Для остальных интерфейсов напрямую возвращается baseClient, что эквивалентно поведению по умолчанию
        return baseClient;
    }
}
```

> Если Bean `CallFactoryConfigurer` не зарегистрирован, поведение компонента остаётся полностью неизменным.

---

Предыдущий: [Пользовательский конвертер данных](converter.md) | Следующий: [Конфигурация таймаутов на уровне метода](timeout.md)