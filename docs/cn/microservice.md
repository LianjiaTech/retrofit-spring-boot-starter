# 微服务之间的 HTTP 调用
[English](../en/microservice.md) | **简体中文** | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

本组件支持微服务场景下的 HTTP 调用，通过 `serviceId` 替代硬编码的 `baseUrl`，实现服务发现与负载均衡。

## 实现 ServiceInstanceChooser

用户可以自行实现 `ServiceInstanceChooser` 接口，完成服务实例的选取逻辑，并将其配置成 Spring Bean。对于 Spring Cloud 应用，可以使用如下实现：

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

[上一节：GraalVM Native Image / AOT 支持](aot.md) | [下一节：自定义 RetrofitClient 注解](custom-annotation.md)