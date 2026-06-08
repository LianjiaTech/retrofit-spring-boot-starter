# 方法级超时配置
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | **繁體中文** | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

元件支援透過 `@Timeout` 注解在方法或類級別設定超时參數，覆蓋全域超时配置。

## 優先級鏈

```
方法 @Timeout → 類 @Timeout → 全域配置（GlobalTimeoutProperty）
```

- `@Timeout` 屬性預設值 `-1` 表示"未配置，繼承上層優先級鏈"
- 設為 `0` 表示"無超时"
- 設為正數表示具體超时毫秒數
- `-1` 是 OkHttp 超时的非法值域（OkHttp 只接受 0 和正數），用作"未配置"標記不會與合法超时值衝突

## 四種超时維度

| 屬性 | 含義 | 預設值 |
|------|------|--------|
| `connectTimeoutMs` | 連線超时（毫秒） | `-1`（繼承上層） |
| `readTimeoutMs` | 讀取超时（毫秒） | `-1`（繼承上層） |
| `writeTimeoutMs` | 寫入超时（毫秒） | `-1`（繼承上層） |
| `callTimeoutMs` | 完整呼叫超时（毫秒） | `-1`（繼承上層） |

## 類級 @Timeout

在介面上宣告 `@Timeout`，為該介面的所有方法設定超时：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 方法級 @Timeout

在特定方法上宣告 `@Timeout`，覆蓋類級或全域超时配置：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 繼承類級 readTimeoutMs = 5000
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 方法級覆蓋：慢查詢介面使用更長超时
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 僅方法級 @Timeout（無類級注解）

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 使用全域超时配置
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 僅此方法使用自定义超时
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 實作機制

- 類級 `@Timeout`：在 OkHttpClient 建立時處理，零執行時開銷
- 方法級 `@Timeout`：由 `TimeoutCallFactory` 預建立 per-method OkHttpClient clone，執行時透過 Invocation tag 查找，無注解介面零額外開銷

---

[上一節：自定义 OkHttpClient](okhttp-client.md) | [下一節：日志打印](logging.md)