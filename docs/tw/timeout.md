# 方法級超時設定
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | **繁體中文** | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

元件支援透過 `@Timeout` 註解在方法或類級別設定超時參數，覆寫全域超時設定。

## 優先級鏈

```
方法 @Timeout → 類 @Timeout → 全域設定（GlobalTimeoutProperty）
```

- `@Timeout` 屬性預設值 `-1` 表示「未設定，繼承上層優先級鏈」
- 設為 `0` 表示「無超時」
- 設為正數表示具體超時毫秒數
- `-1` 是 OkHttp 超時的非法值域（OkHttp 只接受 0 和正數），用作「未設定」標記不會與合法超時值衝突

## 四種超時維度

| 屬性 | 含義 | 預設值 |
|------|------|--------|
| `connectTimeoutMs` | 連線超時（毫秒） | `-1`（繼承上層） |
| `readTimeoutMs` | 讀取超時（毫秒） | `-1`（繼承上層） |
| `writeTimeoutMs` | 寫入超時（毫秒） | `-1`（繼承上層） |
| `callTimeoutMs` | 完整呼叫超時（毫秒） | `-1`（繼承上層） |

## 類級 @Timeout

在介面上宣告 `@Timeout`，為該介面的所有方法設定超時：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 方法級 @Timeout

在特定方法上宣告 `@Timeout`，覆寫類級或全域超時設定：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 繼承類級 readTimeoutMs = 5000
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 方法級覆寫：慢查詢介面使用更長超時
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 僅方法級 @Timeout（無類級註解）

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 使用全域超時設定
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 僅此方法使用自訂超時
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

[上一節：自訂 OkHttpClient](okhttp-client.md) | [下一節：日誌打印](logging.md)