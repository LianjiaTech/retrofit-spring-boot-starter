# Monitoreo de metricas (Micrometer)
[English](../en/metrics.md) | [简体中文](../cn/metrics.md) | [繁體中文](../tw/metrics.md) | [日本語](../ja/metrics.md) | [한국어](../ko/metrics.md) | **Español** | [Türkçe](../tr/metrics.md) | [Русский](../ru/metrics.md)

El componente tiene capacidad de recoleccion de metricas integrada basada en [Micrometer](https://micrometer.io/). **Esta deshabilitada por defecto**, se debe establecer explicitamente `retrofit.metrics.enable=true` para habilitarla.

> **Por que esta deshabilitada por defecto y requiere habilitacion explicita**: No existe una restriccion confiable de orden de carga entre autoconfiguraciones de Spring Boot, depender de `@ConditionalOnBean(MeterRegistry.class)` para habilitacion automatica causaria fallas silenciosas debido al momento de evaluacion, donde "el usuario introduce actuator pero no hay metricas". Con habilitacion opt-in, el comportamiento es completamente predecible: introducir actuator no se instrumenta automaticamente; al habilitar explicitamente, si no existe `MeterRegistry` en el contenedor, el inicio falla rapidamente en lugar de quedar silenciosamente sin metricas.

## Modo de habilitacion

1. Introducir Micrometer y el backend de monitoreo correspondiente (Prometheus / Datadog / Atlas etc). Spring Boot Actuator registrara `MeterRegistry`:

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

2. Habilitar explicitamente en la configuracion:

```yaml
retrofit:
  metrics:
    enable: true
```

## Metricas recolectadas

| Nombre de metrica | Tipo | Descripcion |
|---|---|---|
| `retrofit.client.requests` | Timer | Distribucion de duracion de cada llamada HTTP (incluyendo percentiles y histograma SLO) |
| `retrofit.client.requests.active` | LongTaskTimer | Numero de solicitudes en curso y tiempo maximo de vida |
| `retrofit.client.errors` | Counter | Contador de excepciones de solicitudes (dimension por nombre de clase de excepcion) |

## Dimensiones de tags

Tags por defecto (cardinalidad limitada, seguros para backends sensibles a alta cardinalidad como Prometheus):

| Tag | Descripcion | Ejemplo de valor |
|---|---|---|
| `client` | Nombre de clase simple de la interfaz Retrofit | `UserService` |
| `method` | Nombre del metodo Java | `getUser` |
| `http.method` | Metodo HTTP | `GET`/`POST` |
| `uri` | Template de ruta en la anotacion (no expande `@Path`) | `user/{id}` |
| `status` | Bucket de codigo de estado | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | Resultado del negocio | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | Solo para metrica errors, nombre de clase de excepcion | `SocketTimeoutException` |

> **Nota**: los valores de tags deben ser conjuntos con cardinalidad limitada, por lo que el tag `uri` usa el template de ruta de la anotacion (con placeholder `{id}`) en lugar de la URL real expandida. Esto evita la explosion de cardinalidad de metricas causada por parametros de ruta dinamicos.

## Configuracion

```yaml
retrofit:
  metrics:
    # Si habilitar, por defecto false. Se debe establecer explicitamente a true para装配ar el interceptor de metrics.
    enable: true
    # Percentiles publicados por Timer; array vacio significa no publicar
    percentiles: [0.5, 0.95, 0.99]
    # Buckets de histograma SLO; array vacio significa no publicar histograma
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # Si incluir tag host, por defecto deshabilitado (en escenarios de baseUrl dinamico, la cantidad de hosts puede ser grande)
      host: false
      # Si incluir tag uri, por defecto habilitado
      uri: true
    # Tags globales estaticos adicionales
    extra-tags:
      app: my-service
      env: prod
    # Prefijo del nombre de metrica, por defecto retrofit.client
    metric-name-prefix: retrofit.client
```

## Tags personalizados

Si las dimensiones de tags por defecto no satisfacen las necesidades, se puede implementar la interfaz `RetrofitTagsProvider` y registrarla como Spring Bean, lo que sobrescribirá automaticamente la implementacion por defecto:

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

> En implementaciones personalizadas, se debe asegurar que: el conjunto de valores de tags tenga cardinalidad limitada, y el orden y nombre de tags sean estables, de lo contrario Micrometer creara multiples Meters sin sentido, causando desperdicio de memoria.

---

[Anterior: Decodificador de errores](error-decoder.md) | [Siguiente: Actuator Endpoint](actuator.md)