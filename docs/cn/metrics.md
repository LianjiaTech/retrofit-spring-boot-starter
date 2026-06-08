# 指标监控（Micrometer）
[English](../en/metrics.md) | **简体中文** | [繁體中文](../tw/metrics.md) | [日本語](../ja/metrics.md) | [한국어](../ko/metrics.md) | [Español](../es/metrics.md) | [Türkçe](../tr/metrics.md) | [Русский](../ru/metrics.md)

组件内置了基于 [Micrometer](https://micrometer.io/) 的指标采集能力。**默认关闭**，需要显式设置 `retrofit.metrics.enable=true` 才会启用。

> **为什么是默认关闭、显式开启**：Spring Boot autoconfig 之间没有可靠的加载顺序约束，依赖 `@ConditionalOnBean(MeterRegistry.class)` 自动启用会因求值时机问题导致"用户引入 actuator 却没有指标"的隐性失败。改为 opt-in 后行为完全可预期：用户引入 actuator 不会被自动埋点；显式开启时若容器内没有 `MeterRegistry`，启动会快速失败而非静默无指标。

## 启用方式

1. 引入 Micrometer 与对应的监控后端（Prometheus / Datadog / Atlas 等）。Spring Boot Actuator 会注册 `MeterRegistry`：

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

2. 在配置中显式开启：

```yaml
retrofit:
  metrics:
    enable: true
```

## 采集的指标

| 指标名 | 类型 | 含义 |
|---|---|---|
| `retrofit.client.requests` | Timer | 每次 HTTP 调用耗时分布（含分位数与 SLO 直方图） |
| `retrofit.client.requests.active` | LongTaskTimer | 进行中的请求数与最长存活时间 |
| `retrofit.client.errors` | Counter | 请求异常计数（按 exception 类名维度） |

## 标签维度

默认 tag（基数有界，可放心用于 Prometheus 等高基数敏感后端）：

| Tag | 含义 | 取值示例 |
|---|---|---|
| `client` | Retrofit 接口的简单类名 | `UserService` |
| `method` | Java 方法名 | `getUser` |
| `http.method` | HTTP 方法 | `GET`/`POST` |
| `uri` | 注解上的路径模板（不展开 `@Path`） | `user/{id}` |
| `status` | 状态码桶 | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | 业务结果 | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | 仅 errors 指标，异常类名 | `SocketTimeoutException` |

> **注意**：tag 取值必须是有界集合，因此 `uri` 标签使用注解上的路径模板（含 `{id}` 占位符），而非展开后的实际 URL。这样可以避免动态路径参数导致的指标基数爆炸。

## 配置项

```yaml
retrofit:
  metrics:
    # 是否启用，默认 false。需要显式设置为 true 才会装配 metrics 拦截器。
    enable: true
    # Timer 发布的分位数；空数组表示不发布
    percentiles: [0.5, 0.95, 0.99]
    # SLO 直方图分桶；空数组表示不发布直方图
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # 是否带 host 标签，默认关闭（动态 baseUrl 场景下 host 数量可能很大）
      host: false
      # 是否带 uri 标签，默认开启
      uri: true
    # 全局静态附加标签
    extra-tags:
      app: my-service
      env: prod
    # 指标名前缀，默认 retrofit.client
    metric-name-prefix: retrofit.client
```

## 自定义标签

如果默认的 tag 维度不满足需求，可以实现 `RetrofitTagsProvider` 接口并注册为 Spring Bean，将自动覆盖默认实现：

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

> 自定义实现时务必保证：tag 取值集合有界、tag 顺序与名称稳定，否则会导致 Micrometer 创建多个无意义的 Meter，造成内存浪费。

---

[上一节：错误解码器](error-decoder.md) | [下一节：Actuator Endpoint](actuator.md)