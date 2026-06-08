# Reintento de solicitudes
[English](../en/retry.md) | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | [한국어](../ko/retry.md) | **Español** | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

El componente soporta reintento global y reintento declarativo.

## Reintento global

El reintento global esta deshabilitado por defecto, la configuracion por defecto es:

```yaml
retrofit:
  global-retry:
    # Si habilitar reintento global
    enable: false
    # Intervalo base de reintento global (milisegundos)
    interval-ms: 100
    # Numero maximo de reintentos global
    max-retries: 2
    # Estrategia de retroceso: FIXED (intervalo fijo, por defecto) / EXPONENTIAL (retroceso exponencial)
    backoff-strategy: fixed
    # Limite superior del intervalo de retroceso exponencial (milisegundos), solo efectivo para EXPONENTIAL
    max-interval-ms: 30000
    # Coeficiente de fluctuacion [0.0, 1.0], 0.0 significa sin fluctuacion
    jitter: 0.0
    # Reglas de reintento global
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception
```

### Reglas de reintento

Las reglas de reintento soportan tres configuraciones:

1. **RESPONSE_STATUS_NOT_2XX**: ejecutar reintento cuando el codigo de estado de respuesta no es 2xx
2. **OCCUR_IO_EXCEPTION**: ejecutar reintento cuando ocurre una excepcion IO
3. **OCCUR_EXCEPTION**: ejecutar reintento cuando ocurre cualquier excepcion

### Estrategia de retroceso y fluctuacion

`backoffStrategy` controla el crecimiento del intervalo de reintento, por defecto `FIXED` que es consistente con el comportamiento historico:

- **FIXED**: el intervalo de cada reintento es fijo, igual a `intervalMs`
- **EXPONENTIAL**: retroceso exponencial, el intervalo del N-esimo reintento = `intervalMs × 2^N` (N empieza desde 0), limitado por `maxIntervalMs` para evitar crecimiento infinito

`jitter` (rango `[0.0, 1.0]`, por defecto `0.0` sin fluctuacion) agrega fluctuacion aleatoria sobre la demora calculada, evitando el efecto de estampida causado por reintentos sincronicos de multiples clientes:

> Demora real = demora calculada × (1 + jitter × random), donde random es un numero aleatorio en `[0, 1)`

### Activacion condicional: por codigo de estado / tipo de excepcion

Sobre las reglas de grano grueso de `RetryRule`, se puede limitar adicionalmente las condiciones de activacion (por defecto vacio, consistente con el comportamiento historico):

- `retryStatusCodes`: solo reintentar cuando el codigo de estado de respuesta coincide con la lista (requiere la regla `RESPONSE_STATUS_NOT_2XX`). Por ejemplo `{502, 503, 504}`
- `retryExceptionClasses`: solo reintentar cuando el tipo de excepcion coincide con la lista (limita adicionalmente sobre las excepciones que coinciden con `RetryRule`). Por ejemplo `{SocketTimeoutException.class}`

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

Si solo algunas solicitudes necesitan reintento, se puede usar la anotacion `@Retry` en las interfaces o metodos correspondientes.

## Extension personalizada

Si se necesita modificar el comportamiento del reintento, se puede heredar `RetryInterceptor` y configurarlo como un Spring Bean.

---

[Anterior: Registro de logs](logging.md) | [Siguiente: Interceptores](interceptor.md)