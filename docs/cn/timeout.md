# 方法级超时配置
[English](../en/timeout.md) | **简体中文** | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

组件支持通过 `@Timeout` 注解在方法或类级别设置超时参数，覆盖全局超时配置。

## 优先级链

```
方法 @Timeout → 类 @Timeout → 全局配置（GlobalTimeoutProperty）
```

- `@Timeout` 属性默认值 `-1` 表示"未配置，继承上层优先级链"
- 设为 `0` 表示"无超时"
- 设为正数表示具体超时毫秒数
- `-1` 是 OkHttp 超时的非法值域（OkHttp 只接受 0 和正数），用作"未配置"标记不会与合法超时值冲突

## 四种超时维度

| 属性 | 含义 | 默认值 |
|------|------|--------|
| `connectTimeoutMs` | 连接超时（毫秒） | `-1`（继承上层） |
| `readTimeoutMs` | 读取超时（毫秒） | `-1`（继承上层） |
| `writeTimeoutMs` | 写入超时（毫秒） | `-1`（继承上层） |
| `callTimeoutMs` | 完整调用超时（毫秒） | `-1`（继承上层） |

## 类级 @Timeout

在接口上声明 `@Timeout`，为该接口的所有方法设置超时：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 方法级 @Timeout

在特定方法上声明 `@Timeout`，覆盖类级或全局超时配置：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 继承类级 readTimeoutMs = 5000
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 方法级覆盖：慢查询接口使用更长超时
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 仅方法级 @Timeout（无类级注解）

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 使用全局超时配置
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 仅此方法使用自定义超时
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

[上一节：自定义 OkHttpClient](okhttp-client.md) | [下一节：日志打印](logging.md)