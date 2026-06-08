# マイクロサービス間の HTTP 呼び出し
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | **日本語** | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

本コンポーネントはマイクロサービスシーンでの HTTP 呼び出しをサポートし、`serviceId` でハードコードされた `baseUrl` を置き換え、サービスディスカバリとロードバランシングを実現します。

## ServiceInstanceChooser の実装

ユーザーは `ServiceInstanceChooser` インターフェースを実装してサービスインスタンスの選択ロジックを完成し、Spring Bean として設定できます。Spring Cloud アプリケーションでは、以下の実装を使用できます：

```java
@Service
public class SpringCloudServiceInstanceChooser implements ServiceInstanceChooser {

    private LoadBalancerClient loadBalancerClient;

    @Autowired
    public SpringCloudServiceInstanceChooser(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }

    @Override
    public URI choose(String serviceId) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        Assert.notNull(serviceInstance, "can not found service instance! serviceId=" + serviceId);
        return serviceInstance.getUri();
    }
}
```

## serviceId と path の指定

`serviceId` と `path` で `baseUrl` を置き換えます：

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[前節：GraalVM Native Image / AOT サポート](aot.md) | [次節：RetrofitClient アノテーションのカスタマイズ](custom-annotation.md)