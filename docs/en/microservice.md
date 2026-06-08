# HTTP Calls Between Microservices
**English** | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

This component supports HTTP calls in microservice scenarios, using `serviceId` instead of a hardcoded `baseUrl` to achieve service discovery and load balancing.

## Implement ServiceInstanceChooser

Users can implement the `ServiceInstanceChooser` interface to provide service instance selection logic and configure it as a Spring Bean. For Spring Cloud applications, the following implementation can be used:

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

## Specify serviceId and path

Use `serviceId` and `path` instead of `baseUrl`:

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[Previous: Error Decoder](error-decoder.md) | [Next: Custom RetrofitClient Annotation](custom-annotation.md)