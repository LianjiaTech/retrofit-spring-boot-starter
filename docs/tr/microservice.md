# Mikro Servisler Arasi HTTP Cagrisi
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | **Türkçe** | [Русский](../ru/microservice.md)

Bu bilesen mikro servis senaryosundaki HTTP cagrisini destekler, `serviceId` ile sabit kodlanmis `baseUrl` yerine servis kesfi ve yuk dengelemesi gerceklestirir.

## ServiceInstanceChooser Gerceklestirme

Kullanici `ServiceInstanceChooser` arayuzunu gerceklestirerek servis ornegini secme mantigini tamamlayip Spring Bean olarak yapilandirabilir. Spring Cloud uygulamasi icin su gerceklestirim kullanilabilir:

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

## serviceId ve path Belirleme

`serviceId` ve `path` ile `baseUrl` yerine kullanin:

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[Onceki: GraalVM Native Image / AOT Destegi](aot.md) | [Sonraki: RetrofitClient Ek Aciklamasi](custom-annotation.md)