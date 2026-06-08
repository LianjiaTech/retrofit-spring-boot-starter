# 마이크로서비스간 HTTP 호출
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | **한국어** | [Español](../es/microservice.md) | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

이 컴포넌트는 마이크로서비스 시나리오下의 HTTP 호출을 지원합니다. `serviceId`로 하드코딩된 `baseUrl`을 대체하여, 서비스 발견과 로드 밸런싱을 구현합니다.

## ServiceInstanceChooser 구현

사용자는 `ServiceInstanceChooser` 인터페이스를自行으로 구현하여, 서비스 인스턴스 선택 로직을 완성하고 Spring Bean으로 구성할 수 있습니다. Spring Cloud 애플리케이션의 경우, 다음 구현을 사용할 수 있습니다:

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

## serviceId와 path 지정

`serviceId`와 `path`로 `baseUrl`을 대체합니다:

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[이전: 오류 디코더](error-decoder.md) | [다음: 커스텀 RetrofitClient 어노테이션](custom-annotation.md)