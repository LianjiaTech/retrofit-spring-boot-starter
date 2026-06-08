# Actuator Endpoint (曝光 RetrofitClient метаданных)
[English](../en/actuator.md) | [简体中文](../cn/actuator.md) | [繁體中文](../tw/actuator.md) | [日本語](../ja/actuator.md) | [한국어](../ko/actuator.md) | [Español](../es/actuator.md) | [Türkçe](../tr/actuator.md) | **Русский**

Компонент предоставляет只读ный Endpoint на основе Spring Boot Actuator, который через `/actuator/retrofit`曝光ает полную конфигурационную метаданную всех `@RetrofitClient` интерфейсов в приложении, что облегчает排查 вопросов "какой baseUrl / таймаут / логирование / повторные попытки / circuit breaker конфигурация действительно действуют для данного интерфейса".

> **Опциональная зависимость, включение по необходимости**: Endpoint собирается только когда пользователь引入ает actuator (`@ConditionalOnClass`); проекты Spring Boot 3 без actuator не受到影响 и нормально启动ют. Экспозиция и включение/выключение Endpoint полностью передаются стандартной конфигурации management Spring Boot (`@ConditionalOnAvailableEndpoint`), без кастомных переключателей.

## Включение

1. Добавьте actuator:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. Экспозиция retrofit endpoint (по умолчанию actuator экспонирует только `health`, нужно явно добавить):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## Способы доступа

| Запрос | Описание |
|---|---|
| `GET /actuator/retrofit` | Список всех client + секция `global` глобальной конфигурации + `count` |
| `GET /actuator/retrofit/{полное квалифицированное имя интерфейса}` | Запрос отдельного client по полному квалифицированному имени интерфейса, 404 если не найдено |

## Пример структуры ответа

```json
{
  "count": 2,
  "global": {
    "enableErrorDecoder": true,
    "globalConverterFactories": ["retrofit2.converter.jackson.JacksonConverterFactory"],
    "timeout": { "connectMs": 10000, "readMs": 10000, "writeMs": 10000, "callMs": 0 },
    "connectionPool": { "maxIdleConnections": 5, "keepAliveDurationMs": 300000 },
    "log":     { "enable": false, "logLevel": "INFO", "logStrategy": "BASIC", "aggregate": true },
    "retry":   { "enable": false, "maxRetries": 2, "intervalMs": 100,
                 "backoffStrategy": "FIXED", "maxIntervalMs": 30000, "jitter": 0.0,
                 "retryStatusCodes": [], "retryExceptionClasses": [],
                 "retryRules": ["RESPONSE_STATUS_NOT_2XX", "OCCUR_IO_EXCEPTION"] },
    "degrade": { "degradeType": "none",
                 "sentinel":     { "enable": false, "ruleCount": 0 },
                 "resilience4j": { "enable": false, "circuitBreakerConfigName": "defaultCircuitBreakerConfig" } },
    "metrics": { "enable": false, "metricNamePrefix": "retrofit.client", "tagHost": false, "tagUri": true }
  },
  "clients": [{
    "beanName": "userService",
    "interfaceName": "com.example.UserService",
    "baseUrl": "${test.baseUrl}",
    "resolvedBaseUrl": "http://localhost:8080/api/user/",
    "serviceId": null,
    "path": null,
    "converterFactories": [],
    "callAdapterFactories": [],
    "fallback": null,
    "fallbackFactory": null,
    "errorDecoder": "com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder$DefaultErrorDecoder",
    "validateEagerly": false,
    "sourceOkHttpClient": null,
    "timeoutEffective": true,
    "timeout": { "connectMs": 3000, "readMs": 3000, "writeMs": 10000, "callMs": 0,
                 "inheritedFields": ["writeMs", "callMs"] },
    "pool":    { "maxIdleConnections": 5, "keepAliveDurationMs": 300000,
                 "inheritedFields": ["maxIdleConnections", "keepAliveDurationMs"] },
    "logging": { "source": "interface", "enable": true, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## Описание семантики字段

- **`resolvedBaseUrl`**: 最终 разрешенный baseUrl. Значение доступно только когда интерфейс уже внедрен и используется (触发ает инстанцирование), иначе `null` (baseUrl разрешается лениво, не预先 разрешается до触发ения).
- **`inheritedFields` для `timeout` / `pool`**: Когда соответствующее поле конфигурации `@RetrofitClient` установлено в `-1` (значение по умолчанию), это означает "复用овать глобальную конфигурацию". Endpoint разрешает `-1` в глобальное fallback-значение по правилам, согласованным с реальным构建ением, и记录ает эти поля в `inheritedFields`, что облегчает区分ение "явная конфигурация интерфейса" или "наследуется от глобальной".
- **`timeoutEffective`**: `false` когда интерфейс指定ает кастомный OkHttpClient через `sourceOkHttpClient` (в этом случае таймауты/пул соединений определяются源客户端ом, `timeout`/`pool` не отображаются).
- **`source` для `logging` / `retry`**:
  - `"interface"`: На интерфейсе есть аннотация `@Logging` / `@Retry`, остальные поля -- развернутые значения аннотации;
  - `"global"`: Интерфейс без соответствующей аннотации, откатывается к глобальной конфигурации при выполнении, значения не повторяются, см. верхнюю секцию `global`.
  - Примечание: `@Logging` / `@Retry` на уровне метода не отображаются здесь на детальном уровне (аннотации метода при выполнении приоритетнее интерфейса, интерфейс приоритетнее глобальной конфигурации).
- **`degrade.enabled`**: Значение из `RetrofitDegrade.isEnableDegrade(интерфейс)`; `type` -- глобальный `degrade.degrade-type` (`none` / `sentinel` / `resilience4j`).
- **`fallback` / `fallbackFactory`**: `null` когда не配置ено (по умолчанию `void.class`).

---

[Предыдущая: Метрики (Micrometer)](metrics.md) | [Следующая: GraalVM Native Image / AOT](aot.md)