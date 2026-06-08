# Invocacion HTTP entre microservicios
[English](../en/microservice.md) | [简体中文](../cn/microservice.md) | [繁體中文](../tw/microservice.md) | [日本語](../ja/microservice.md) | [한국어](../ko/microservice.md) | **Español** | [Türkçe](../tr/microservice.md) | [Русский](../ru/microservice.md)

Este componente soporta invocaciones HTTP en escenarios de microservicios, usando `serviceId` en lugar de `baseUrl` codificado directamente, para lograr descubrimiento de servicios y balance de carga.

## Implementar ServiceInstanceChooser

El usuario puede implementar la interfaz `ServiceInstanceChooser`, completar la logica de seleccion de instancias de servicio, y configurarla como Spring Bean. Para aplicaciones Spring Cloud, se puede usar la siguiente implementacion:

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

[Anterior: Soporte GraalVM Native Image / AOT](aot.md) | [Siguiente: Anotacion RetrofitClient personalizada](custom-annotation.md)