# Circuit Breaker / Degradacion
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | **Español** | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

La degradacion con circuit breaker esta deshabilitada por defecto. Actualmente soporta dos implementaciones: **Sentinel** y **Resilience4j**.

```yaml
retrofit:
  degrade:
    # Tipo de degradacion, por defecto none significa no habilitado
    degrade-type: sentinel
```

## Sentinel

### Agregar dependencia

Agregar manualmente la dependencia de Sentinel:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### Degradacion declarativa

Configurar `degrade-type=sentinel` para habilitar, luego declarar la anotacion `@SentinelDegrade` en las interfaces o metodos relevantes:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = SentinelFallbackUserService.class)
@SentinelDegrade(rules = {
    @SentinelDegradeRule(grade = 0, count = 100, timeWindow = 4),
    @SentinelDegradeRule(grade = 1, count = 0.01, timeWindow = 3)
})
public interface SentinelUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @SentinelDegrade(rules = {@SentinelDegradeRule(grade = 2, count = 1, timeWindow = 6)})
    User getUser(@Query("id") Long id);
}
```

### Degradacion global con Sentinel

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # Estrategia de degradacion (0: tiempo de respuesta promedio; 1: proporcion de excepciones; 2: cantidad de excepciones)
      - grade: 0
        # Umbral correspondiente a cada estrategia. Tiempo de respuesta promedio(ms), proporcion de excepciones(0-1), cantidad de excepciones(1-N)
        count: 1000
        # Duracion del circuit breaker, en segundos
        time-window: 5
        # Numero minimo de solicitudes que pueden activar el circuit breaker (dentro del rango de tiempo de estadistica efectiva)
        min-request-amount: 5
        # Umbral de proporcion de solicitudes lentas en modo RT
        slow-ratio-threshold: 1.0
        # Duracion del intervalo de estadistica, en milisegundos
        stat-interval-ms: 1000
```

## Resilience4j

### Agregar dependencia

Agregar manualmente la dependencia de Resilience4j:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### Registrar configuracion de circuit breaker

Implementar la interfaz `CircuitBreakerConfigRegistrar` para registrar `CircuitBreakerConfig`:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Reemplazar la configuracion por defecto de CircuitBreakerConfig
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Registrar otras CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### Degradacion declarativa

Configurar `degrade-type=resilience4j` para habilitar, luego declarar `@Resilience4jDegrade` en las interfaces o metodos relevantes:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = Resilience4jFallbackFactory.class)
@Resilience4jDegrade(circuitBreakerConfigName = "testCircuitBreakerConfig")
public interface Resilience4jUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @Resilience4jDegrade(enable = false)
    User getUser(@Query("id") Long id;
}
```

### Degradacion global con Resilience4j

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Obtiene CircuitBreakerConfig de CircuitBreakerConfigRegistry segun este nombre, como configuracion global de circuit breaker
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

Se puede especificar `CircuitBreakerConfig` mediante `circuitBreakerConfigName`, incluyendo `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` o `@Resilience4jDegrade.circuitBreakerConfigName`.

## Extension de circuit breaker / degradacion

Si se necesita usar otra implementacion de circuit breaker / degradacion, heredar `BaseRetrofitDegrade` y configurarlo como un Spring Bean.

## Fallback y FallbackFactory

Si `@RetrofitClient` no establece `fallback` o `fallbackFactory`, cuando se activa el circuit breaker se lanzara directamente una excepcion `RetrofitBlockException`. El usuario puede personalizar el valor de retorno del metodo durante la degradacion estableciendo `fallback` o `fallbackFactory`.

> Nota: la clase `fallback` debe ser una clase de implementacion de la interfaz actual, `fallbackFactory` debe ser una clase de implementacion de `FallbackFactory<T>`, con el tipo de parametro generico siendo la interfaz actual. Ademas, las instancias de `fallback` y `fallbackFactory` deben configurarse como Spring Beans.

La principal diferencia de `fallbackFactory` respecto a `fallback` es la capacidad de percibir la causa de la excepcion (cause) de cada activacion del circuit breaker.

### Ejemplo de Fallback

```java
@Slf4j
@Service
public class HttpDegradeFallback implements HttpDegradeApi {

    @Override
    public Result<Integer> test() {
        Result<Integer> fallback = new Result<>();
        fallback.setCode(100)
                .setMsg("fallback")
                .setBody(1000000);
        return fallback;
    }
}
```

### Ejemplo de FallbackFactory

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("Circuit breaker activado! ", cause.getMessage(), cause);
        return new HttpDegradeApi() {
            @Override
            public Result<Integer> test() {
                Result<Integer> fallback = new Result<>();
                fallback.setCode(100)
                        .setMsg("fallback")
                        .setBody(1000000);
                return fallback;
            }
        };
    }
}
```

---

[Anterior: Interceptores](interceptor.md) | [Siguiente: Decodificador de errores](error-decoder.md)