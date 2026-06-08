# メソッドレベルタイムアウト設定
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | **日本語** | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

コンポーネントは `@Timeout` アノテーションでメソッドまたはクラスレベルのタイムアウトパラメーターを設定し、グローバルタイムアウト設定をオーバーライドできます。

## 優先度チェーン

```
メソッド @Timeout → クラス @Timeout → グローバル設定（GlobalTimeoutProperty）
```

- `@Timeout` 属性のデフォルト値 `-1` は「未設定、上位優先度チェーンを継承」を意味する
- `0` は「タイムアウトなし」を意味する
- 正数は具体的なタイムアウトミリ秒を意味する
- `-1` は OkHttp タイムアウトの不正値域（OkHttp は 0 と正数のみ受け付ける）であり、「未設定」のマークとして合法なタイムアウト値と衝突しない

## 4つのタイムアウトディメンション

| 属性 | 意味 | デフォルト値 |
|------|------|--------|
| `connectTimeoutMs` | 接続タイムアウト（ミリ秒） | `-1`（上位継承） |
| `readTimeoutMs` | 読取タイムアウト（ミリ秒） | `-1`（上位継承） |
| `writeTimeoutMs` | 書込タイムアウト（ミリ秒） | `-1`（上位継承） |
| `callTimeoutMs` | 完全呼出タイムアウト（ミリ秒） | `-1`（上位継承） |

## クラスレベル @Timeout

インターフェースに `@Timeout` を宣言し、当該インターフェースの全メソッドのタイムアウトを設定します：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## メソッドレベル @Timeout

特定メソッドに `@Timeout` を宣言し、クラスレベルまたはグローバルタイムアウト設定をオーバーライドします：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // クラスレベル readTimeoutMs = 5000 を継承
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // メソッドレベルオーバーライド：低速クエリインターフェースはより長いタイムアウトを使用
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## メソッドレベル @Timeout のみ（クラスレベルアノテーションなし）

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // グローバルタイムアウト設定を使用
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // このメソッドのみカスタムタイムアウトを使用
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

[前へ：カスタム OkHttpClient](okhttp-client.md) | [次へ：ログ出力](logging.md)