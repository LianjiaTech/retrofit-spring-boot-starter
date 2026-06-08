# Mikroservisler Arası HTTP Çağrı
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | **Türkçe** | [Русский](../ru/microservice.md)

Bu bileşen, mikroservis senaryosundaki HTTP çağrıyı destekler ve `serviceId` ile sabit kodlanmış `baseUrl` yerine hizmet keşfi ve yük dengeleme gerçekleştirir.

## ServiceInstanceChooser Uygulama

Kullanıcılar, `ServiceInstanceChooser` arayüzünü uygulayarak hizmet örneği seçim mantığını tamamlayabilir ve Spring Bean olarak yapılandırabilir. Spring Cloud uygulamaları için aşağıdaki uygulama kullanılabilir:

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

## serviceId ve path Belirtme

`baseUrl` yerine `serviceId` ve `path` kullanın:

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[Önceki: Hata Dekoderi](error-decoder.md) | [Sonraki: Özel RetrofitClient Anotasyonu](custom-annotation.md)