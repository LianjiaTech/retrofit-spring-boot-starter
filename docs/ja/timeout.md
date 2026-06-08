# メソッドレベルのタイムアウト設定
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | **日本語** | [한국어](../ko/timeout.md) | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

コンポーネントは `@Timeout` アノテーションを使用してメソッドまたはクラスレベルでタイムアウトパラメータを設定し、グローバルタイムアウト設定をオーバーライドすることをサポートしています。

## 優先度チェーン

```
メソッド @Timeout → クラス @Timeout → グローバル設定（GlobalTimeoutProperty）
```

- `@Timeout` 属性のデフォルト値 `-1` は「未設定、上位優先度チェーンを継承」を意味します
- `0` は「タイムアウトなし」を意味します
- 正数は具体的なタイムアウト時間（ミリ秒）を意味します
- `-1` は OkHttp タイムアウトの不正値範囲（OkHttp は 0 と正数のみ受け付け）であり、「未設定」マークとして使用しても合法なタイムアウト値と衝突しません

## 4つのタイムアウトディメンション

| 属性 | 意味 | デフォルト値 |
|------|------|--------|
| `connectTimeoutMs` | 接続タイムアウト（ミリ秒） | `-1`（上位継承） |
| `readTimeoutMs` | 読み取りタイムアウト（ミリ秒） | `-1`（上位継承） |
| `writeTimeoutMs` | 書き込みタイムアウト（ミリ秒） | `-1`（上位継承） |
| `callTimeoutMs` | 完全呼び出しタイムアウト（ミリ秒） | `-1`（上位継承） |

## クラスレベル @Timeout

インターフェースに `@Timeout` を宣言すると、そのインターフェースの全メソッドにタイムアウトを設定します：

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## メソッドレベル @Timeout

特定のメソッドに `@Timeout` を宣言すると、クラスレベルまたはグローバルタイムアウト設定をオーバーライドします：

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

## 実装メカニズム

- クラスレベル `@Timeout`：OkHttpClient 作成時に処理。ランタイムオーバーヘッドなし
- メソッドレベル `@Timeout`：`TimeoutCallFactory` が per-method OkHttpClient clone を事前作成。ランタイムでは Invocation tag で検索。アノテーションなしインターフェースは追加オーバーヘッドなし

---

[前節：OkHttpClient のカスタマイズ](okhttp-client.md) | [次節：ログ出力](logging.md)