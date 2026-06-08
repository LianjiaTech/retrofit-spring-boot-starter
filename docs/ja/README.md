# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | **日本語** | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

**[Retrofit](https://square.github.io/retrofit/) は HTTP API を Java インターフェースとして定義できるライブラリです。本コンポーネントは Retrofit と Spring Boot を深く統合し、様々な実用機能の強化をサポートしています。**

- **Spring Boot 3.x/4.x プロジェクト**の場合、retrofit-spring-boot-starter **4.x** を使用してください
  - Spring Boot 4.x はデフォルトで Jackson 3 を使用しますが、本コンポーネントのデフォルト Converter は Jackson 2 を使用するため、**4.x プロジェクトではグローバル Converter を Jackson 3 に設定することを推奨します**
  - 設定方法：`retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x プロジェクト**の場合、retrofit-spring-boot-starter **2.x** を使用してください。Spring Boot 1.4.2 以上のバージョンをサポートします

> プロジェクトは継続的に最適化・改良されています。ISSUE や PR の提出をお待ちしています！star を付けていただけることが、継続的な更新への最大のサポートとなります！

GitHub：[https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee：[https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## クイックスタート

### 依存関係の追加

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

依存関係を追加すればすぐに使用できます。問題が発生した場合は、[よくある質問](faq.md)を参照してください。

### HTTP インターフェースの定義

**インターフェースには `@RetrofitClient` アノテーションを付与する必要があります！**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * IDに基づいてユーザー名を検索
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> 注意：**メソッドのリクエストパスで `/` から始まる表記は慎重に使用してください**。Retrofit のパス結合ルール：`baseUrl = http://localhost:8080/api/test/` の場合、メソッドパス `person` の完全パスは `http://localhost:8080/api/test/person` ですが、メソッドパス `/person` の完全パスは `http://localhost:8080/person` となります。

### インジェクションして使用

インターフェースを他の Service にインジェクションして使用します：

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // userService を呼び出す
    }
}
```

### HTTP リクエストアノテーション

HTTP リクエスト関連のアノテーションは、すべて Retrofit のオリジナルアノテーションを使用します：

| アノテーション分類 | サポートされるアノテーション |
|----------|-----------|
| リクエスト方式 | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| リクエストヘッダー | `@Header` `@HeaderMap` `@Headers` |
| Query パラメータ | `@Query` `@QueryMap` `@QueryName` |
| Path パラメータ | `@Path` |
| Form パラメータ | `@Field` `@FieldMap` `@FormUrlEncoded` |
| リクエストボディ | `@Body` |
| ファイルアップロード | `@Multipart` `@Part` `@PartMap` |
| URL パラメータ | `@Url` |

> 詳細は [Retrofit 公式ドキュメント](https://square.github.io/retrofit/)を参照してください

## 機能特性

- [x] [HTTP レスポンス自動適応](response-adaptation.md)
- [x] [カスタムデータ変換器](converter.md)
- [x] [カスタム OkHttpClient と Call.Factory SPI](okhttp-client.md)
- [x] [メソッドレベルタイムアウト設定](timeout.md)
- [x] [ログ出力](logging.md)
- [x] [リクエストリトライ](retry.md)
- [x] [インターセプター](interceptor.md)
- [x] [サーキットブレーカー/フェイルバック](degrade.md)
- [x] [エラーデコーダー](error-decoder.md)
- [x] [マイクロサービス間の HTTP 呼び出し](microservice.md)
- [x] [カスタム RetrofitClient アノテーション](custom-annotation.md)
- [x] [全設定項目参考](configuration.md)
- [x] [その他の機能例](examples.md)
- [x] [よくある質問](faq.md)

## フィードバック

何か問題がある場合は、issue の提出や QQ グループへの参加でフィードバックをお願いします。

グループ番号：806714302

![QQグループ画像](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)