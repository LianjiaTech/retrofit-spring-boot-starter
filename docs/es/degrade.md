# Circuit breaker/degradación
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | [한국어](../ko/degrade.md) | **Español** | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

El circuit breaker/degradación está deshabilitado por defecto. Actualmente se admiten dos implementaciones: **Sentinel** y **Resilience4j**.

```yaml
retrofit:
  degrade:
    # Tipo de circuit breaker/degradación, predeterminado none significa no habilitar
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

### Circuit breaker declarativo

Configurar `degrade-type=sentinel` para habilitar, luego declarar la anotación `@SentinelDegrade` en las interfaces o métodos correspondientes:

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

### Circuit breaker/degradación global de Sentinel

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
    rules:
      # Estrategia de degradación (0: tiempo de respuesta promedio; 1: proporción de excepciones; 2: cantidad de excepciones)
      - grade: 0
        # Umbral correspondiente a cada estrategia de degradación. Tiempo de respuesta promedio(ms), proporción de excepciones(0-1), cantidad de excepciones(1-N)
        count: 1000
        # Duración del circuit breaker, en s
        time-window: 5
        # Número mínimo de peticiones que pueden trigger el circuit breaker (dentro del rango de tiempo estadístico válido)
        min-request-amount: 5
        # Umbral de ratio de peticiones lentas en modo RT
        slow-ratio-threshold: 1.0
        # Duración del intervalo estadístico, en milisegundos
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

### Registrar configuración de circuit breaker

Implementar la interfaz `CircuitBreakerConfigRegistrar` para registrar `CircuitBreakerConfig`:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // Reemplazar el CircuitBreakerConfig predeterminado
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // Registrar otros CircuitBreakerConfig
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### Circuit breaker declarativo

Configurar `degrade-type=resilience4j` para habilitar, luego declarar `@Resilience4jDegrade` en las interfaces o métodos correspondientes:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = Resilience4jFallbackFactory.class)
@Resilience4jDegrade(circuitBreakerConfigName = "testCircuitBreakerConfig")
public interface Resilience4jUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @Resilience4jDegrade(enable = false)
    User getUser(@Query("id") Long id);
}
```

### Circuit breaker/degradación global de Resilience4j

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # Obtener CircuitBreakerConfig desde CircuitBreakerConfigRegistry según este nombre, como configuración global de circuit breaker
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

Especificar `CircuitBreakerConfig` mediante `circuitBreakerConfigName`, incluyendo `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` o `@Resilience4jDegrade.circuitBreakerConfigName`.

## Extender circuit breaker/degradación

Si se necesita usar otra implementación de circuit breaker/degradación, heredar `BaseRetrofitDegrade` y configurarlo como bean de Spring.

## Fallback y FallbackFactory

Si `@RetrofitClient` no establece `fallback` o `fallbackFactory`, cuando se trigger el circuit breaker se lanzará directamente una excepción `RetrofitBlockException`. Los usuarios pueden personalizar el valor de retorno del método durante el circuit breaker estableciendo `fallback` o `fallbackFactory`.

> Nota: la clase `fallback` debe ser una clase de implementación de la interfaz actual, `fallbackFactory` debe ser una clase de implementación de `FallbackFactory<T>`, con el tipo de parámetro genérico como el tipo de la interfaz actual. Además, las instancias de `fallback` y `fallbackFactory` deben configurarse como beans de Spring.

La diferencia principal entre `fallbackFactory` y `fallback` es que `fallbackFactory` puede感知 la causa de la excepción (cause) de cada circuit breaker.

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

[Anterior: Interceptor](interceptor.md) | [Siguiente: Decodificador de errores](error-decoder.md)