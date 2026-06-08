# 微服務之間的 HTTP 呼叫
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | **繁體中文** | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

本元件支援微服務場景下的 HTTP 呼叫，透過 `serviceId` 替代硬編碼的 `baseUrl`，實作服務發現與負載均衡。

## 實作 ServiceInstanceChooser

使用者可以自行實作 `ServiceInstanceChooser` 介面，完成服務實例的選取邏輯，並將其設定成 Spring Bean。對於 Spring Cloud 應用，可以使用如下實作：

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

## 指定 serviceId 和 path

使用 `serviceId` 和 `path` 替代 `baseUrl`：

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[上一節：錯誤解碼器](error-decoder.md) | [下一節：自訂 RetrofitClient 註解](custom-annotation.md)