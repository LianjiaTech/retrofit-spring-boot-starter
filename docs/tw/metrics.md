# 指标监控（Micrometer）
[English](../en/metrics.md) | [简体中文](../cn/metrics.md) | **繁體中文** | [日本語](../ja/metrics.md) | [한국어](../ko/metrics.md) | [Español](../es/metrics.md) | [Türkçe](../tr/metrics.md) | [Русский](../ru/metrics.md)

元件內建了基於 [Micrometer](https://micrometer.io/) 的指标採集能力。**預設關閉**，需要明確設定 `retrofit.metrics.enable=true` 才會啟用。

> **為什麼是預設關閉、明確開啟**：Spring Boot autoconfig 之間沒有可靠的載入順序約束，依賴 `@ConditionalOnBean(MeterRegistry.class)` 自動啟用會因求值时机問題導致"使用者引入 actuator 却沒有指标"的隐性失败。改為 opt-in 後行為完全可預期：使用者引入 actuator 不會被自動埋點；明確開啟时若容器內沒有 `MeterRegistry`，啟動会快速失败而非靜默無指标。

## 啟用方式

1. 引入 Micrometer 与對應的監控後端（Prometheus / Datadog / Atlas 等）。Spring Boot Actuator 会注册 `MeterRegistry`：

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

2. 在配置中明確開啟：

```yaml
retrofit:
  metrics:
    enable: true
```

## 採集的指标

| 指标名 | 型別 | 含義 |
|---|---|---|
| `retrofit.client.requests` | Timer | 每次 HTTP 呼叫耗時分布（含分位數与 SLO 直方圖） |
| `retrofit.client.requests.active` | LongTaskTimer | 进行中的請求數与最长存活時間 |
| `retrofit.client.errors` | Counter | 請求例外計數（按 exception 類名维度） |

## 标签维度

預設 tag（基数有界，可放心用於 Prometheus 等高基数敏感後端）：

| Tag | 含義 | 取值示例 |
|---|---|---|
| `client` | Retrofit 介面的簡單類名 | `UserService` |
| `method` | JAVA 方法名 | `getUser` |
| `http.method` | HTTP 方法 | `GET`/`POST` |
| `uri` | 注解上的路徑模板（不展開 `@Path`） | `user/{id}` |
| `status` | 狀態碼桶 | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | 業務结果 | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | 僅 errors 指标，例外類名 | `SocketTimeoutException` |

> **注意**：tag 取值必須是有界集合，因此 `uri` 标签使用注解上的路徑模板（含 `{id}` 佔位符），而非展開後的實際 URL。這樣可以避免動態路徑參數導致的指标基数爆炸。

## 配置項

```yaml
retrofit:
  metrics:
    # 是否啟用，預設 false。需要明確設定為 true 才會裝配 metrics 拦截器。
    enable: true
    # Timer 發佈的分位數；空陣列表示不發佈
    percentiles: [0.5, 0.95, 0.99]
    # SLO 直方圖分桶；空陣列表示不發佈直方圖
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # 是否带 host 标签，預設關閉（動態 baseUrl 場景下 host 數量可能很大）
      host: false
      # 是否带 uri 标签，預設開啟
      uri: true
    # 全域靜態附加标签
    extra-tags:
      app: my-service
      env: prod
    # 指标名前缀，預設 retrofit.client
    metric-name-prefix: retrofit.client
```

## 自定义标签

如果預設的 tag 维度不滿足需求，可以實作 `RetrofitTagsProvider` 介面並注册為 Spring Bean，將自動覆蓋預設實作：

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

> 自定义實作时務必保證：tag 取值集合有界、tag 順序与名稱穩定，否則会導致 Micrometer 建立多個無意義的 Meter，造成記憶體浪費。

---

[上一節：错误解码器](error-decoder.md) | [下一節：Actuator Endpoint](actuator.md)