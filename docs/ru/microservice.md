# HTTP-вызовы между микросервисами
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | [Español](../es/microservice.md) | [Türkçe](../tr/microservice.md) | **Русский**

Данный компонент поддерживает HTTP-вызовы в микросервисных сценариях, заменяя жестко заданный `baseUrl` на `serviceId` для реализации обнаружения сервисов и балансировки нагрузки.

## Реализация ServiceInstanceChooser

Пользователь может自行но реализовать интерфейс `ServiceInstanceChooser` для完成ения логики выбора сервисного экземпляра и зарегистрировать его как Spring Bean. Для приложений Spring Cloud можно использовать следующую реализацию:

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

## Указание serviceId и path

Используйте `serviceId` и `path` вместо `baseUrl`:

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[Предыдущая: GraalVM Native Image / AOT](aot.md) | [Следующая: Кастомные аннотации RetrofitClient](custom-annotation.md)