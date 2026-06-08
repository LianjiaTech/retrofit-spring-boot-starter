# Referencia completa de configuracion
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | **Español** | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

El componente soporta multiples propiedades configurables para adaptarse a diferentes escenarios de negocio. A continuacion se muestran todas las propiedades de configuracion y sus valores por defecto:

```yaml
retrofit:
  # Fabricas de convertidores globales (por defecto JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Fabricas de adaptadores globales (las CallAdapterFactory extendidas por el componente ya estan integradas, no las configure repetidamente)
  global-call-adapter-factories:
    # ...

  # Configuracion global de registro de logs
  global-log:
    # Habilitar registro de logs (por defecto false)
    enable: false
    # Nivel global de registro de logs
    log-level: info
    # Estrategia global de registro de logs (por defecto BASIC)
    log-strategy: basic
    # Si agregar logs de solicitud
    aggregate: true
    # Nombre del log, por defecto es el nombre completo de la clase LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Headers de solicitud sensibles que deben ocultarse en los logs
    # Por defecto se ocultan: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Nota: la configuracion del usuario sobrescribirá completamente el valor por defecto, se deben incluir los items que se desea mantener ocultos
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # Configuracion global de reintento
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

  # Configuracion global de timeouts
  global-timeout:
    # Timeout global de lectura (milisegundos)
    read-timeout-ms: 10000
    # Timeout global de escritura (milisegundos)
    write-timeout-ms: 10000
    # Timeout global de conexion (milisegundos)
    connect-timeout-ms: 10000
    # Timeout global de llamada completa (milisegundos), 0 significa sin timeout
    call-timeout-ms: 0

  # Configuracion global de pool de conexiones
  global-connection-pool:
    # Numero maximo de conexiones idle
    max-idle-connections: 5
    # Duracion de mantenimiento de conexion (milisegundos)
    keep-alive-duration-ms: 300000

  # Configuracion de monitoreo de metricas (por defecto deshabilitado; requiere enable=true explicito para装配ar, y debe existir MeterRegistry en el contenedor)
  metrics:
    # Si habilitar, por defecto false
    enable: false
    # Percentiles de Timer
    percentiles: [0.5, 0.95, 0.99]
    # Buckets de histograma SLO
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # Si incluir tag host
      host: false
      # Si incluir tag uri
      uri: true
    # Tags globales adicionales
    extra-tags:
      app: my-service
    # Prefijo del nombre de metrica
    metric-name-prefix: retrofit.client

  # Configuracion de circuit breaker / degradacion
  degrade:
    # Tipo de circuit breaker / degradacion. Por defecto none, significa no habilitado
    degrade-type: none
    # Configuracion global de degradacion con Sentinel
    global-sentinel-degrade:
      # Si habilitar
      enable: false
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
    # Configuracion global de degradacion con Resilience4j
    global-resilience4j-degrade:
      # Si habilitar
      enable: false
      # Obtiene CircuitBreakerConfig de CircuitBreakerConfigRegistry segun este nombre, como configuracion global de circuit breaker
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # Establecer automaticamente el scope de PathMatchInterceptor como prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # Si habilitar funcionalidad de ErrorDecoder
  enable-error-decoder: true
```

En la mayoria de escenarios, agregar la configuracion anterior en el archivo de configuracion de Spring Boot (application.yml o application.properties) permite modificar personalizar la funcionalidad del componente. Si la configuracion no surte efecto, consultar [Preguntas frecuentes](faq.md).

**Si el archivo de configuracion de Spring Boot no puede surtir efecto, se puede configurar manualmente el bean RetrofitProperties**, el codigo es:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // Modificar manualmente los valores de configuracion de retrofitProperties
    return retrofitProperties;
}
```

---

[Anterior: Anotacion RetrofitClient personalizada](custom-annotation.md) | [Siguiente: Otros ejemplos de funcionalidad](examples.md)