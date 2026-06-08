# 全量配置项参考
[English](../en/configuration.md) | **简体中文** | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | [한국어](../ko/configuration.md) | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

组件支持多个可配置属性，用来应对不同的业务场景。以下是所有配置属性及默认值：

```yaml
retrofit:
  # 全局转换器工厂（默认 JacksonConverterFactory）
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # 全局适配器工厂（组件扩展的 CallAdapterFactory 已内置，请勿重复配置）
  global-call-adapter-factories:
    # ...

  # 全局日志打印配置
  global-log:
    # 启用日志打印（默认 false）
    enable: false
    # 全局日志打印级别
    log-level: info
    # 全局日志打印策略（默认 BASIC）
    log-strategy: basic
    # 是否聚合打印请求日志
    aggregate: true
    # 日志名称，默认为 LoggingInterceptor 的全类名
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 日志中需要隐藏的敏感请求头
    # 默认遮蔽：Authorization、Proxy-Authorization、Cookie、Set-Cookie
    # 注意：用户配置该项会整体覆盖默认值，需自行包含仍要遮蔽的项
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # 全局重试配置
  global-retry:
    # 是否启用全局重试
    enable: false
    # 全局重试基础间隔时间（毫秒）
    interval-ms: 100
    # 全局最大重试次数
    max-retries: 2
    # 退避策略：FIXED（固定间隔，默认）/ EXPONENTIAL（指数退避）
    backoff-strategy: fixed
    # 指数退避间隔上限（毫秒），仅 EXPONENTIAL 生效
    max-interval-ms: 30000
    # 抖动系数 [0.0, 1.0]，0.0 表示无抖动
    jitter: 0.0
    # 全局重试规则
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # 全局超时时间配置
  global-timeout:
    # 全局读取超时时间（毫秒）
    read-timeout-ms: 10000
    # 全局写入超时时间（毫秒）
    write-timeout-ms: 10000
    # 全局连接超时时间（毫秒）
    connect-timeout-ms: 10000
    # 全局完整调用超时时间（毫秒），0 表示无超时
    call-timeout-ms: 0

  # 全局连接池配置
  global-connection-pool:
    # 最大空闲连接数
    max-idle-connections: 5
    # 保持连接时长（毫秒）
    keep-alive-duration-ms: 300000

  # 指标监控配置（默认关闭；需要显式 enable=true 才会装配，且容器内必须有 MeterRegistry）
  metrics:
    # 是否启用，默认 false
    enable: false
    # Timer 分位数
    percentiles: [0.5, 0.95, 0.99]
    # SLO 直方图分桶
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # 是否带 host 标签
      host: false
      # 是否带 uri 标签
      uri: true
    # 全局附加标签
    extra-tags:
      app: my-service
    # 指标名前缀
    metric-name-prefix: retrofit.client

  # 熔断降级配置
  degrade:
    # 熔断降级类型。默认 none，表示不启用熔断降级
    degrade-type: none
    # 全局 Sentinel 降级配置
    global-sentinel-degrade:
      # 是否开启
      enable: false
      rules:
        # 降级策略（0：平均响应时间；1：异常比例；2：异常数量）
        - grade: 0
          # 各降级策略对应的阈值。平均响应时间(ms)，异常比例(0-1)，异常数量(1-N)
          count: 1000
          # 熔断时长，单位为 s
          time-window: 5
          # （在有效统计时间范围内）能够触发熔断的最小请求数
          min-request-amount: 5
          # RT 模式下慢请求率的阈值
          slow-ratio-threshold: 1.0
          # 时间间隔统计持续时间，单位为毫秒
          stat-interval-ms: 1000
    # 全局 Resilience4j 降级配置
    global-resilience4j-degrade:
      # 是否开启
      enable: false
      # 根据该名称从 CircuitBreakerConfigRegistry 获取 CircuitBreakerConfig，作为全局熔断配置
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # 自动设置 PathMatchInterceptor 的 scope 为 prototype
  auto-set-prototype-scope-for-path-math-interceptor: true
  # 是否开启 ErrorDecoder 功能
  enable-error-decoder: true
```

绝大部分场景下，在 Spring Boot 配置文件（application.yml 或者 application.properties）中加上上述配置，即可自定义修改组件功能。如遇配置无法生效等问题，参见[常见问题](faq.md)。

**如果 Spring Boot 配置文件无法生效，可以手动配置 RetrofitProperties Bean**，代码如下：

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // 手动修改 retrofitProperties 各项配置值
    return retrofitProperties;
}
```

---

[上一节：自定义 RetrofitClient 注解](custom-annotation.md) | [下一节：其他功能示例](examples.md)