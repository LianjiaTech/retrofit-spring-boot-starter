# retrofit-spring-boot-starter

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | **日本語** | [한국어](../ko/README.md) | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)

**[retrofit](https://square.github.io/retrofit/) は HTTP API を Java インターフェースに変換するライブラリです。本コンポーネントは Retrofit と SpringBoot を深く統合し、さまざまな実用的な機能強化をサポートしています。**

- **Spring Boot 3.x/4.x プロジェクトの場合、retrofit-spring-boot-starter 4.x を使用してください**
    - Spring Boot 4.x はデフォルトで jackson3 を使用しますが、本コンポーネントのデフォルト converter は jackson2 です。**Spring Boot 4.x プロジェクトでは、グローバル converter を jackson3 に設定することを推奨します**
    - 設定方法: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x プロジェクトの場合、[retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x) を使用してください**。Spring Boot 1.4.2 以降をサポートしています。

## クイックスタート

### 依存関係の追加

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

ほとんどの Spring Boot プロジェクトでは、依存関係を追加するだけで使用できます。

### HTTP Java インターフェースの定義

**インターフェースには `@RetrofitClient` アノテーションを付ける必要があります！**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

   /**
    * ID でユーザー名を検索
    */
   @POST("getName")
   String getName(@Query("id") Long id);
}
```

> 注意：**メソッドの要求パスの先頭に `/` を使用する場合は注意が必要です**。Retrofit では、`baseUrl=http://localhost:8080/api/test/` の場合、メソッドの要求パスが `person` なら、完全な要求パスは `http://localhost:8080/api/test/person` になります。メソッドの要求パスが `/person` なら、完全な要求パスは `http://localhost:8080/person` になります。

### インジェクションして使用

**インターフェースを他の Service にインジェクションして使用できます！**

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
       // call userService
    }
}
```

## 機能特性

- [HTTP レスポンス結果の自動適応](response-adaptation.md)
- [データコンバータのカスタマイズ](converter.md)
- [OkHttpClient と Call.Factory SPI のカスタマイズ](okhttp-client.md)
- [メソッドレベルのタイムアウト設定](timeout.md)
- [ログ出力](logging.md)
- [リクエストリトライ](retry.md)
- [インターセプタ](interceptor.md)
- [サーキットブレーカ / デグレード](degrade.md)
- [エラーデコーダ](error-decoder.md)
- [メトリクス監視（Micrometer）](metrics.md)
- [Actuator Endpoint](actuator.md)
- [GraalVM Native Image / AOT サポート](aot.md)
- [マイクロサービス間の HTTP 呼び出し](microservice.md)
- [RetrofitClient アノテーションのカスタマイズ](custom-annotation.md)
- [全設定項目リファレンス](configuration.md)
- [その他の機能例](examples.md)
- [よくある質問](faq.md)