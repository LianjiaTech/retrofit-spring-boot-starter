# Llamadas HTTP entre microservicios
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | **Español** | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

Este componente admite llamadas HTTP en escenarios de microservicios, usando `serviceId` en lugar del `baseUrl` hardcodeado, para lograr descubrimiento de servicios y balanceo de carga.

## Implementar ServiceInstanceChooser

Los usuarios pueden implementar la interfaz `ServiceInstanceChooser` para completar la lógica de selección de instancias de servicio, y configurarla como bean de Spring. Para aplicaciones Spring Cloud, se puede usar la siguiente implementación:

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

## Especificar serviceId y path

Usar `serviceId` y `path` en lugar de `baseUrl`:

```java
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

---

[Anterior: Decodificador de errores](error-decoder.md) | [Siguiente: Anotación RetrofitClient personalizada](custom-annotation.md)