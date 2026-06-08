# Reintento de petición
[English](../en/retry.md) | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | **Español** | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

El componente admite reintento global y reintento declarativo.

## Reintento global

El reintento global está deshabilitado por defecto, la configuración predeterminada es la siguiente:

```yaml
retrofit:
  global-retry:
    # Si habilitar reintento global
    enable: false
    # Intervalo base de reintento global (milisegundos)
    interval-ms: 100
    # Número máximo de reintentos global
    max-retries: 2
    # Estrategia de retroceso: FIXED (intervalo fijo, predeterminado) / EXPONENTIAL (retroceso exponencial)
    backoff-strategy: fixed
    # Límite superior del intervalo de retroceso exponencial (milisegundos), solo aplica con EXPONENTIAL
    max-interval-ms: 30000
    # Factor de jitter [0.0, 1.0], 0.0 significa sin jitter
    jitter: 0.0
    # Reglas de reintento global
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### Reglas de reintento

Las reglas de reintento admiten tres configuraciones:

1. **RESPONSE_STATUS_NOT_2XX**: ejecuta reintento cuando el código de estado de respuesta no es 2xx
2. **OCCUR_IO_EXCEPTION**: ejecuta reintento cuando ocurre una excepción IO
3. **OCCUR_EXCEPTION**: ejecuta reintento cuando ocurre cualquier excepción

### Estrategia de retroceso y jitter

`backoffStrategy` controla cómo crece el intervalo de reintento, por defecto `FIXED` es consistente con el comportamiento histórico:

- **FIXED**: cada intervalo de reintento es fijo como `intervalMs`
- **EXPONENTIAL**: retroceso exponencial, el intervalo del N-ésimo reintento = `intervalMs × 2^N` (N empieza desde 0), con `maxIntervalMs` como límite superior para evitar que el intervalo crezca indefinidamente

`jitter` (valor `[0.0, 1.0]`, predeterminado `0.0` sin jitter) se usa para agregar jitter aleatorio sobre la demora calculada, evitando el efecto de estampido causado por múltiples clientes reintentando sincrónicamente:

> Demora real = demora calculada × (1 + jitter × random), donde random es un número aleatorio en `[0, 1)`

### Trigger condicional: por código de estado / tipo de excepción

Sobre las reglas de grano grueso de `RetryRule`, se puede进一步restringir las condiciones de trigger (predeterminado vacío, consistente con el comportamiento histórico):

- `retryStatusCodes`: solo reintenta cuando el código de estado de respuesta coincide con la lista (requiere la regla `RESPONSE_STATUS_NOT_2XX`). Por ejemplo `{502, 503, 504}`
- `retryExceptionClasses`: solo reintenta cuando el tipo de excepción coincide con la lista (restringe further sobre las excepciones que coinciden con `RetryRule`). Por ejemplo `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Reintento declarativo

Si solo algunas peticiones necesitan reintento, se puede usar la anotación `@Retry` en las interfaces o métodos correspondientes.

## Extensión personalizada

Si se necesita modificar el comportamiento de reintento de petición, se puede heredar `RetryInterceptor` y configurarlo como un bean de Spring.

---

[Anterior: Log](logging.md) | [Siguiente: Interceptor](interceptor.md)