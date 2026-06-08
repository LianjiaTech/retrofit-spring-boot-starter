# インターセプター
[English](../en/interceptor.md) | [简体中文](../cn/interceptor.md) | [繁體中文](../tw/interceptor.md) | **日本語** | [한국어](../ko/interceptor.md) | [Español](../es/interceptor.md) | [Türkçe](../tr/interceptor.md) | [Русский](../ru/interceptor.md)

本コンポーネントは4つのインターセプター機構を提供し、様々な場面の HTTP リクエストインターセプトニーズに対応します。

## グローバルアプリケーションインターセプター

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

## グローバルネットワークインターセプター

`NetworkInterceptor` インターフェースを実装し、Spring Bean として設定します。

## アノテーション式パスマッチングインターセプター

多くの場面で、特定の HTTP インターフェースに対してのみ特殊なロジックを実行する必要があります。この場合、パスマッチングインターセプターを使用することで、エレガントに実現できます。

### BasePathMatchInterceptor を継承してインターセプトハンドラーを記述

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

### インターフェースで @Intercept を使用してマーク

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
// 複数のパスマッチングインターセプターを使用する場合は、@Intercept を追加即可
public interface InterceptorUserService {

    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);
}
```

上記の `@Intercept` 設定は：`InterceptorUserService` インターフェースの `/api/user/**` パス下（`/api/user/getUser` を除外）のリクエストをインターセプトし、インターセプトハンドラーとして `PathMatchInterceptor` を使用することを意味します。複数のインターセプターを使用する場合は、インターフェースに複数の `@Intercept` アノテーションをマークします。

## カスタムインターセプターアノテーション

「インターセプトアノテーション」に動的にパラメーターを渡し、インターセプト時にそのパラメーターを使用する必要がある場合、「カスタムインターセプターアノテーション」を使用できます。手順は以下の通りです：

1. カスタムアノテーションを作成し、`@InterceptMark` でマーク必須、アノテーションには `include`、`exclude`、`handler` フィールドを含める必要
2. `BasePathMatchInterceptor` を継承してインターセプトハンドラーを記述
3. インターフェースでカスタムアノテーションを使用

以下に「リクエストヘッダーに `accessKeyId`、`accessKeySecret` 署名情報を動的に追加」する例で、完全な流れを説明します。

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

`@Sign` アノテーションで使用するインターセプターとして `SignInterceptor` を指定しています。

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

インターセプターの `accessKeyId` と `accessKeySecret` フィールド値は、`@Sign` アノテーションの `accessKeyId()` と `accessKeySecret()` の値に基づいて自動注入されます。`@Sign` でプレースホルダー形式の文字列を指定した場合、設定プロパティ値から取得して注入します。

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

[前へ：リクエストリトライ](retry.md) | [次へ：サーキットブレーカー/フェイルバック](degrade.md)