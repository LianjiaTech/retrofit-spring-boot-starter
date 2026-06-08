# Интерцепторы
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | [日本語](../ja/interceptor.md) | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | **Русский**

Компонент предоставляет четыре механизма интерцепторов для удовлетворения различных сценариев перехвата HTTP-запросов.

## Глобальные интерцепторы приложения

Когда необходимо выполнить единообразную обработку перехвата для всех HTTP-запросов системы, реализуйте интерфейс `GlobalInterceptor` и зарегистрируйте его как Spring Bean:

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Добавить заголовок "global" в ответ
        return response.newBuilder().header("global", "true").build();
    }
}
```

## Глобальные сетевые интерцепторы

Реализуйте интерфейс `NetworkInterceptor` и зарегистрируйте его как Spring Bean.

## Интерцепторы路径匹配 с аннотациями

В многих сценариях требуется выполнить определённую логику только для некоторых HTTP-интерфейсов. В этом случае можно использовать интерцепторы路径匹配 для элегантной реализации этой функции.

### Наследование BasePathMatchInterceptor для написания обработчика перехвата

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // Добавить заголовок "path.match" в ответ
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### Использование @Intercept на интерфейсе

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// Если нужно использовать несколько интерцепторов路径匹配, просто добавьте дополнительные @Intercept
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

Приведённая выше конфигурация `@Intercept` означает: перехват запросов по пути `/api/user/**` (исключая `/api/user/getUser`) под интерфейсом `InterceptorUserService`, используя обработчик перехвата `PathMatchInterceptor`. Если нужно использовать несколько интерцепторов, просто добавьте несколько аннотаций `@Intercept` на интерфейсе.

## Пользовательская аннотация интерцептора

Иногда нужно динамически передавать некоторые параметры в "аннотацию перехвата" и затем использовать их при перехвате. В этом случае можно использовать "пользовательскую аннотацию интерцептора", шаги следующие:

1. Создать пользовательскую аннотацию, которая должна быть отмечена `@InterceptMark`, и аннотация должна включать поля `include`, `exclude`, `handler`
2. Наследовать `BasePathMatchInterceptor` для написания обработчика перехвата
3. Использовать пользовательскую аннотацию на интерфейсе

Ниже показан полный процесс на примере "динамического добавления информации подписи `accessKeyId`, `accessKeySecret` в заголовки запроса".

### Пользовательская аннотация @Sign

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

В аннотации `@Sign` указан используемый интерцептор — `SignInterceptor`.

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

> Примечание: поля `accessKeyId` и `accessKeySecret` должны иметь методы `setter`.

Значения полей `accessKeyId` и `accessKeySecret` интерцептора автоматически внедряются на основе значений `accessKeyId()` и `accessKeySecret()` аннотации `@Sign`. Если `@Sign` указывает строку в формате placeholder, то значение свойства конфигурации используется для внедрения.

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

Предыдущий: [Повторные попытки запроса](retry.md) | Следующий: [Обрыв цепи/деградация](degrade.md)