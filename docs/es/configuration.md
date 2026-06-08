# Referencia completa de configuración
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | **Español** | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

El componente admite múltiples propiedades configurables para adaptarse a diferentes escenarios de negocio. A continuación se muestran todas las propiedades de configuración y sus valores predeterminados:

```yaml
retrofit:
  # Fábrica de convertidor global (predeterminado JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # Fábrica de adaptador global (las CallAdapterFactory extendidas por el componente ya están integradas, no configure repetidamente)
  global-call-adapter-factories:
    # ...

  # Configuración de log global
  global-log:
    # Habilitar log (predeterminado false)
    enable: false
    # Nivel de log global
    log-level: info
    # Estrategia de log global (predeterminado BASIC)
    log-strategy: basic
    # Si agregar logs de petición
    aggregate: true
    # Nombre del log, predeterminado es el nombre completo de la clase LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Headers de petición sensibles que deben ocultarse en el log
    # Ocultados por defecto: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Nota: la configuración del usuario sobrescribirá completamente los valores predeterminados, debe incluir los items que aún desea ocultar
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # Configuración de reintento global
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

  # Configuración de timeout global
  global-timeout:
    # Timeout de lectura global (milisegundos)
    read-timeout-ms: 10000
    # Timeout de escritura global (milisegundos)
    write-timeout-ms: 10000
    # Timeout de conexión global (milisegundos)
    connect-timeout-ms: 10000
    # Timeout de llamada completa global (milisegundos), 0 significa sin timeout
    call-timeout-ms: 0

  # Configuración de pool de conexiones global
  global-connection-pool:
    # Número máximo de conexiones inactivas
    max-idle-connections: 5
    # Duración de mantenimiento de conexión (milisegundos)
    keep-alive-duration-ms: 300000

  # Configuración de circuit breaker/degradación
  degrade:
    # Tipo de circuit breaker/degradación. Predeterminado none, significa no habilitar
    degrade-type: none
    # Configuración de degradación global de Sentinel
    global-sentinel-degrade:
      # Si habilitar
      enable: false
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
    # Configuración de degradación global de Resilience4j
    global-resilience4j-degrade:
      # Si habilitar
      enable: false
      # Obtener CircuitBreakerConfig desde CircuitBreakerConfigRegistry según este nombre, como configuración global de circuit breaker
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # Establecer automáticamente el scope de PathMatchInterceptor como prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # Si habilitar la funcionalidad de ErrorDecoder
  enable-error-decoder: true
```

En la mayoría de los escenarios, agregar la configuración anterior en el archivo de configuración de Spring Boot (application.yml o application.properties) permite personalizar las funciones del componente. Si la configuración no funciona, consulte las [Preguntas frecuentes](faq.md).

---

[Anterior: Anotación RetrofitClient personalizada](custom-annotation.md) | [Siguiente: Ejemplos de otras funciones](examples.md)