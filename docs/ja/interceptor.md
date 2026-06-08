# インターセプタ
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | **日本語** | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

本コンポーネントは4種類のインターセプタメカニズムを提供し、さまざまなシーンの HTTP リクエストインターセプト要件に対応します。

## グローバルアプリケーションインターセプタ

システム全体の HTTP リクエストに対して統一的なインターセプト処理を行う必要がある場合、`GlobalInterceptor` インターフェースを実装し、Spring Bean として設定します：

```java
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response の Header に global を追加
        return response.newBuilder().header("global", "true").build();
    }
}
```

## グローバルネットワークインターセプタ

`NetworkInterceptor` インターフェースを実装し、Spring Bean として設定します。

## アノテーション式パスマッチインターセプタ

多くのシーンで、一部の HTTP インターフェースのみに特殊なロジックを適用する必要があります。この場合、パスマッチインターセプタを使用して、この機能を優雅に実現できます。

### BasePathMatchInterceptor を継承してインターセプトハンドラを作成

```java
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response の Header に path.match を追加
        return response.newBuilder().header("path.match", "true").build();
    }
}
```

### インターフェースに @Intercept を付与

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 複数のパスマッチインターセプタが必要な場合は、@Intercept を追加してください
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

上記の `@Intercept` 設定は、`InterceptorUserService` インターフェースの `/api/user/**` パス（`/api/user/getUser` を除外）のリクエストをインターセプトし、インターセプトハンドラとして `PathMatchInterceptor` を使用することを意味します。複数のインターセプタが必要な場合は、インターフェースに複数の `@Intercept` アノテーションを付与してください。

## カスタムインターセプタアノテーション

インターセプトアノテーションに動的にパラメータを渡し、インターセプト時にそれらのパラメータを使用する必要がある場合があります。この場合、「カスタムインターセプタアノテーション」を使用できます。手順は以下の通りです：

1. カスタムアノテーション。`@InterceptMark` でマークする必要があり、アノテーションには `include`、`exclude`、`handler` フィールドを含める必要があります
2. `BasePathMatchInterceptor` を継承してインターセプトハンドラを作成
3. インターフェースでカスタムアノテーションを使用

以下に「リクエストヘッダーに `accessKeyId`、`accessKeySecret` 署名情報を動的に追加する」例で完全な流れを示します。

### カスタム @Sign アノテーション

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {

    String accessKeyId();

    String accessKeySecret();

    String[] include() default {"/**"};

    String[] exclude() default {};

    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
```

`@Sign` アノテーションで使用するインターセプタとして `SignInterceptor` を指定しています。

### SignInterceptor の実装

```java
@Component
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
        Response response = chain.proceed(newReq);
        return response.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
    }
}
```

> 注意：`accessKeyId` と `accessKeySecret` フィールドには `setter` メソッドを提供する必要があります。

インターセプタの `accessKeyId` と `accessKeySecret` フィールド値は、`@Sign` アノテーションの `accessKeyId()` と `accessKeySecret()` 値に基づいて自動的に注入されます。`@Sign` でプレースホルダー形式の文字列が指定された場合、設定プロパティ値が取得されて注入されます。

### インターフェースで @Sign を使用

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    @GET("getAll")
    Response<List<User>> getAll();
}
```

---

[前節：リクエストリトライ](retry.md) | [次節：サーキットブレーカ / デグレード](degrade.md)