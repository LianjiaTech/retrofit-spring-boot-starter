# Metrics Monitoring (Micrometer)
**English** | [简体中文](../cn/metrics.md) | [繁體中文](../tw/metrics.md) | [日本語](../ja/metrics.md) | [한국어](../ko/metrics.md) | [Español](../es/metrics.md) | [Türkçe](../tr/metrics.md) | [Русский](../ru/metrics.md)

The component includes built-in metrics collection capability based on [Micrometer](https://micrometer.io/). **Disabled by default**, you must explicitly set `retrofit.metrics.enable=true` to enable it.

> **Why opt-in instead of auto-detection**: Spring Boot does not guarantee a stable autoconfig load order. Relying on `@ConditionalOnBean(MeterRegistry.class)` for auto-enabling is fragile -- when Retrofit's autoconfig is processed before actuator's `SimpleMetricsExportAutoConfiguration`, the condition silently evaluates to "no MeterRegistry" and the entire metrics module is skipped, leaving users wondering why no Retrofit metrics show up. Switching to opt-in makes the behavior predictable: pulling in actuator does not silently start emitting metrics; explicitly enabling metrics without a `MeterRegistry` in context fails fast at startup instead of going silently uninstrumented.

## How to Enable

1. Add Micrometer along with the registry implementation for your monitoring backend (Prometheus / Datadog / Atlas / etc.). Spring Boot Actuator registers a `MeterRegistry` out of the box:

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

2. Explicitly enable metrics in configuration:

```yaml
retrofit:
  metrics:
    enable: true
```

## Collected Metrics

| Metric Name | Type | Meaning |
|---|---|---|
| `retrofit.client.requests` | Timer | HTTP call duration distribution per invocation (with percentiles and SLO histograms) |
| `retrofit.client.requests.active` | LongTaskTimer | In-flight request count and longest active duration |
| `retrofit.client.errors` | Counter | Request error count (tagged by exception class name dimension) |

## Tag Dimensions

Default tags (bounded cardinality, safe for high-cardinality-sensitive backends like Prometheus):

| Tag | Meaning | Example Value |
|---|---|---|
| `client` | Simple class name of the Retrofit interface | `UserService` |
| `method` | Java method name | `getUser` |
| `http.method` | HTTP method | `GET`/`POST` |
| `uri` | Path template from annotations (does NOT expand `@Path`) | `user/{id}` |
| `status` | Status code bucket | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | Business result | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | Only for errors metric, exception class name | `SocketTimeoutException` |

> **Note**: Tag values must come from a bounded set. Therefore the `uri` tag uses the path template from annotations (with `{id}` placeholders) rather than the expanded actual URL. This prevents metric cardinality explosion caused by dynamic path parameters.

## Configuration

```yaml
retrofit:
  metrics:
    # Whether to enable, default false. Must be explicitly set to true to wire the metrics interceptor.
    enable: true
    # Percentiles to publish for Timer; empty array disables percentiles
    percentiles: [0.5, 0.95, 0.99]
    # SLO histogram bucket boundaries; empty array disables histograms
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # Whether to include host tag, default off (dynamic baseUrl scenarios may have large host counts)
      host: false
      # Whether to include uri tag, default on
      uri: true
    # Global static extra tags
    extra-tags:
      app: my-service
      env: prod
    # Metric name prefix, default retrofit.client
    metric-name-prefix: retrofit.client
```

## Custom Tags

If the default tag dimensions do not meet your needs, you can implement the `RetrofitTagsProvider` interface and register it as a Spring Bean, which will automatically override the default implementation:

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

> When implementing a custom provider, ensure that: tag value sets are bounded, and tag order and names remain stable. Otherwise Micrometer will create multiple meaningless Meters, causing memory waste.

---

[Previous: Error Decoder](error-decoder.md) | [Next: Actuator Endpoint](actuator.md)