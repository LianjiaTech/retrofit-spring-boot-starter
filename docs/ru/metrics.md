# Метрики (Micrometer)
[English](../en/metrics.md) | [简体中文](../cn/metrics.md) | [繁體中文](../tw/metrics.md) | [日本語](../ja/metrics.md) | [한국어](../ko/metrics.md) | [Español](../es/metrics.md) | [Türkçe](../tr/metrics.md) | **Русский**

Компонент имеет встроенную возможность сбора метрик на основе [Micrometer](https://micrometer.io/). **По умолчанию отключена**, необходимо явно установить `retrofit.metrics.enable=true` для активации.

> **Почему по умолчанию отключена и требует явного включения**: Между авто-конфигурациями Spring Boot нет надежного порядка загрузки, зависимость от `@ConditionalOnBean(MeterRegistry.class)` для автоматического включения может привести к скрытому сбоям из-за времени вычисления -- "пользователь добавил actuator, но метрик нет". С opt-in поведением полностью предсказуемо: добавление actuator не приводит к автоматическому埋点у; при явном включении, если в контейнере нет `MeterRegistry`,启动 быстро завершится с ошибкой вместо тихого отсутствия метрик.

## Включение

1. Добавьте Micrometer и соответствующий мониторинговый backend (Prometheus / Datadog / Atlas и др.). Spring Boot Actuator зарегистрирует `MeterRegistry`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. Явно включите в конфигурации:

```yaml
retrofit:
  metrics:
    enable: true
```

## Собираемые метрики

| Имя метрики | Тип | Значение |
|---|---|---|
| `retrofit.client.requests` | Timer | Распределение времени каждого HTTP-вызова (с percentile и SLO histogram) |
| `retrofit.client.requests.active` | LongTaskTimer | Количество выполняющихся запросов и максимальное время жизни |
| `retrofit.client.errors` | Counter | Счетчик исключений запросов (по имени класса exception) |

## Теги (измерения)

Стандартные теги (基数 ограничена, безопасно для использования с Prometheus и другими высокочувствительными к基数 backend):

| Tag | Значение | Пример значения |
|---|---|---|
| `client` | Простое имя класса Retrofit-интерфейса | `UserService` |
| `method` | Имя Java-метода | `getUser` |
| `http.method` | HTTP-метод | `GET`/`POST` |
| `uri` | Шаблон пути из аннотации (без раскрытия `@Path`) | `user/{id}` |
| `status` | Bucket кода состояния | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | Бизнес-результат | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | Только для метрики errors, имя класса исключения | `SocketTimeoutException` |

> **Примечание**: Значения тегов должны быть ограниченным множеством, поэтому тег `uri` использует шаблон пути из аннотации (с placeholder `{id}`), а не развернутый фактический URL. Это позволяет избежать基数爆炸 метрик из-за динамических параметров пути.

## Конфигурация

```yaml
retrofit:
  metrics:
    # Включить ли, по умолчанию false. Нужно явно установить true для сборки interceptor метрик.
    enable: true
    # Percentile для публикации Timer; пустой массив означает не опубликовывать
    percentiles: [0.5, 0.95, 0.99]
    # SLO histogram bucket; пустой массив означает не опубликовывать histogram
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # Добавить ли тег host, по умолчанию отключено (в сценариях динамического baseUrl количество host может быть большим)
      host: false
      # Добавить ли тег uri, по умолчанию включено
      uri: true
    # Глобальные статические дополнительные теги
    extra-tags:
      app: my-service
      env: prod
    # Префикс имени метрики, по умолчанию retrofit.client
    metric-name-prefix: retrofit.client
```

## Кастомные теги

Если стандартные теги не удовлетворяют需求, можно реализовать интерфейс `RetrofitTagsProvider` и зарегистрировать его как Spring Bean, он автоматически переопределит стандартную реализацию:

```java
@Component
public class TenantAwareTagsProvider implements RetrofitTagsProvider {

    private final RetrofitTagsProvider delegate;

    public TenantAwareTagsProvider(MetricsProperty property) {
        this.delegate = new DefaultRetrofitTagsProvider(property);
    }

    @Override
    public Tags getTags(Request request, Response response, Throwable exception) {
        return delegate.getTags(request, response, exception)
                .and("tenant", TenantContext.current());
    }
}
```

> При кастомной реализации обязательно обеспечьте: множество значений тегов ограничено, порядок и имя тегов стабильны, иначе Micrometer создаст多个无意义的 Meter, что приведет к浪费 памяти.

---

[Предыдущая: Декодер ошибок](error-decoder.md) | [Следующая: Actuator Endpoint](actuator.md)