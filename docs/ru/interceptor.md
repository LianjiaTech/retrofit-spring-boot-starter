# Интерцепторы
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | **Русский**

Данный компонент предоставляет четыре типа механизмов интерцепторов для удовлетворения различных сценариев拦截а HTTP-запросов.

## Глобальные интерцепторы приложения

Когда необходимо выполнить единообразную拦截ную обработку всех HTTP-запросов в системе, реализуйте интерфейс `GlobalInterceptor` и зарегистрируйте его как Spring Bean:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Добавить заголовок global в response
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Глобальные сетевые интерцепторы

Реализуйте интерфейс `NetworkInterceptor` и зарегистрируйте его как Spring Bean.

## Интерцепторы路径匹配 по аннотации

В многих случаях нужно выполнять специальную логику только для некоторых HTTP-интерфейсов. В этом случае можно использовать路径匹配 интерцептор для элегантной реализации этой функции.

### Наследование BasePathMatchInterceptor для создания обработчика拦截а

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Добавить заголовок path.match в response
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Аннотация @Intercept на интерфейсе

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// Если нужно использовать несколько路径匹配 интерцепторов, просто добавьте еще @Intercept
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

Конфигурация `@Intercept` выше означает:拦截ать запросы по路径 `/api/user/**` (исключая `/api/user/getUser`) под интерфейсом `InterceptorUserService`, обработчик拦截а -- `PathMatchInterceptor`. Если нужно использовать несколько интерцепторов, просто добавьте несколько аннотаций `@Intercept` на интерфейсе.

## Кастомные аннотации интерцептора

Иногда需要在 аннотации拦截а динамически передавать параметры и使用 их при拦截ании. В этом случае можно использовать "кастомную аннотацию интерцептора",步骤如下:

1. Создать кастомную аннотацию, которая должна быть标记ана `@InterceptMark`, и аннотация должна включать поля `include`, `exclude`, `handler`
2. Наследовать `BasePathMatchInterceptor` для создания обработчика拦截а
3. Использовать кастомную аннотацию на интерфейсе

Ниже приведен пример "динамического добавления `accessKeyId`, `accessKeySecret` информации подписи в заголовки запроса".

### Кастомная аннотация @Sign

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {

    String accessKeyId();

    String accessKeySecret();

    String[] include() default {"/**"};

    String[] exclude() default {};

    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

В аннотации `@Sign` указан используемый интерцептор `SignInterceptor`.

### Реализация SignInterceptor

```java
@Component
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
        Response response = chain.proceed(newReq);
        return response.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
    }
}
```

> Примечание: поля `accessKeyId` и `accessKeySecret` должны иметь `setter`-методы.

Значения полей `accessKeyId` и `accessKeySecret` интерцептора автоматически注入аются из значений `accessKeyId()` и `accessKeySecret()` аннотации `@Sign`. Если `@Sign` указывает строку в формате placeholder, то берется значение свойства конфигурации для注入а.

### Использование @Sign на интерфейсе

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[Предыдущая: Повторные попытки запросов](retry.md) | [Следующая: Circuit Breaker (предохранитель)](degrade.md)