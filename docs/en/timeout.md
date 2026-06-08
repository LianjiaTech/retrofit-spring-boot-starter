# Method-level Timeout Configuration
**English** | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

The component supports setting timeout parameters at the method or class level via the `@Timeout` annotation, overriding global timeout configuration.

## Priority Chain

```
Method @Timeout -> Class @Timeout -> Global Configuration (GlobalTimeoutProperty)
```

- `@Timeout` attribute default value `-1` means "not configured, inherit from upper level of priority chain"
- Setting to `0` means "no timeout"
- Setting to a positive number means a specific timeout in milliseconds
- `-1` is outside OkHttp's valid timeout value range (OkHttp only accepts 0 and positive numbers), so it serves as an "not configured" marker without conflicting with legitimate timeout values

## Four Timeout Dimensions

| Attribute | Meaning | Default |
|-----------|---------|---------|
| `connectTimeoutMs` | Connect timeout (ms) | `-1` (inherit from upper level) |
| `readTimeoutMs` | Read timeout (ms) | `-1` (inherit from upper level) |
| `writeTimeoutMs` | Write timeout (ms) | `-1` (inherit from upper level) |
| `callTimeoutMs` | Full call timeout (ms) | `-1` (inherit from upper level) |

## Class-level @Timeout

Declare `@Timeout` on the interface to set timeouts for all methods of that interface:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## Method-level @Timeout

Declare `@Timeout` on a specific method to override class-level or global timeout configuration:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Inherits class-level readTimeoutMs = 5000
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Method-level override: slow query uses longer timeout
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Method-level @Timeout Only (No Class-level Annotation)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Uses global timeout configuration
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Only this method uses custom timeout
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Implementation Mechanism

- Class-level `@Timeout`: processed at OkHttpClient creation time, zero runtime overhead
- Method-level `@Timeout`: `TimeoutCallFactory` pre-creates per-method OkHttpClient clones, looked up via Invocation tag at runtime; interfaces without annotations have zero extra overhead

---

[Previous: Custom OkHttpClient](okhttp-client.md) | [Next: Logging](logging.md)