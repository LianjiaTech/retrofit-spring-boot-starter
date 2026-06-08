# Actuator Endpoint (exponer metainformacion de RetrofitClient)
[English](../en/actuator.md) | [简体中文](../cn/actuator.md) | [繁體中文](../tw/actuator.md) | [日本語](../ja/actuator.md) | [한국어](../ko/actuator.md) | **Español** | [Türkçe](../tr/actuator.md) | [Русский](../ru/actuator.md)

El componente proporciona un Endpoint de solo lectura basado en Spring Boot Actuator, que expone la configuracion completa de metainformacion de todas las interfaces `@RetrofitClient` de la aplicacion mediante `/actuator/retrofit`, facilitando la investigacion de "que baseUrl / timeout / logs / reintento / circuit breaker es realmente efectivo para una interfaz especifica".

> **Dependencia opcional, habilitacion bajo demanda**: El Endpoint solo se装配a cuando el usuario introduce actuator (`@ConditionalOnClass`), proyectos SpringBoot 3 sin actuator no sufren ningun impacto e inician normalmente. La exposicion y habilitacion del Endpoint se delegan completamente a la configuracion standard de management de Spring Boot (`@ConditionalOnAvailableEndpoint`), sin crear un switch propio.

## Modo de habilitacion

1. Introducir actuator:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. Exponer el retrofit endpoint (por defecto actuator solo expone `health`, se debe agregar explicitamente):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## Modo de acceso

| Solicitud | Descripcion |
|---|---|
| `GET /actuator/retrofit` | Listar todos los clients + segmento de configuracion `global` + `count` |
| `GET /actuator/retrofit/{nombre completo de la interfaz}` | Consultar un client individual por nombre completo de la interfaz, retorna 404 si no coincide |

## Ejemplo de estructura de respuesta

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

## Descripcion semantica de campos

- **`resolvedBaseUrl`**: baseUrl final resuelto. Solo tiene valor cuando la interfaz ha sido inyectada y usada (ha triggered la instanciacion), en caso contrario es `null` (baseUrl se resuelve de forma lazy, no se pre-resuelve sin haber sido triggered).
- **`inheritedFields` de `timeout` / `pool`**: Cuando el campo correspondiente en `@RetrofitClient` esta configurado como `-1` (valor por defecto), significa "reutilizar la configuracion global". El Endpoint解析a `-1` al valor global de fallback segun las mismas reglas de construccion real, y registra los nombres de estos campos en `inheritedFields`, facilitando distinguir entre "configuracion explicita de la interfaz" y "herencia de la configuracion global".
- **`timeoutEffective`**: Cuando la interfaz especifica un OkHttpClient personalizado mediante `sourceOkHttpClient`, es `false` (en este caso timeout/pool de conexiones se determina por el client fuente, `timeout`/`pool` no se muestra).
- **`source` de `logging` / `retry`**:
  - `"interface"`: la interfaz tiene anotacion `@Logging` / `@Retry`, los otros campos son los valores expandidos de la anotacion;
  - `"global"`: la interfaz no tiene la anotacion correspondiente, en tiempo de ejecucion recurre a la configuracion global, en este caso no se expanden repetidamente los valores, consultar el segmento `global` de nivel superior.
  - Nota: `@Logging` / `@Retry` a nivel de metodo no se muestran con detalle aqui (en tiempo de ejecucion la anotacion de metodo tiene prioridad sobre la de interfaz, y la de interfaz tiene prioridad sobre la global).
- **`degrade.enabled`**: Obtenido de `RetrofitDegrade.isEnableDegrade(interfaz)`; `type` es `degrade.degrade-type` global (`none` / `sentinel` / `resilience4j`).
- **`fallback` / `fallbackFactory`**: Cuando no estan configurados (por defecto `void.class`), es `null`.

---

[Anterior: Monitoreo de metricas (Micrometer)](metrics.md) | [Siguiente: Soporte GraalVM Native Image / AOT](aot.md)