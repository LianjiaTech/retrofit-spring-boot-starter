# 日志打印
[English](../en/logging.md) | **简体中文** | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

组件支持全局日志打印和声明式日志打印。

## 全局日志打印

全局日志打印默认关闭（`enable=false`），需要主动开启。开启后默认按 `BASIC` 策略仅打印请求/响应行（含状态码与耗时），开销可忽略。默认配置如下：

```yaml
retrofit:
  global-log:
    # 启用日志打印（默认 false）
    enable: false
    # 全局日志打印级别
    log-level: info
    # 全局日志打印策略（默认 BASIC，仅打印请求/响应行）
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
```

四种日志打印策略含义如下：

1. **NONE**：不打印日志
2. **BASIC**：仅打印请求和响应行
3. **HEADERS**：打印请求和响应行及其请求头/响应头
4. **BODY**：打印请求和响应行、请求头/响应头以及请求体/响应体（如存在）

## 声明式日志打印

如果只需要部分请求才打印日志，可以在相关接口或者方法上使用 `@Logging` 注解。

## 自定义扩展

如果需要修改日志打印行为，可以继承 `LoggingInterceptor`，并将其配置成 Spring Bean。

---

[上一节：方法级超时配置](timeout.md) | [下一节：请求重试](retry.md)