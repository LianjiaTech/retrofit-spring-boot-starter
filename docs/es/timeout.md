# Configuración de timeout por método
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | **Español** | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

El componente admite la configuración de parámetros de timeout en nivel de método o clase mediante la anotación `@Timeout`, sobrescribiendo la configuración global de timeout.

## Cadena de prioridad

```
@Timeout de método → @Timeout de clase → Configuración global (GlobalTimeoutProperty)
```

- El valor predeterminado de las propiedades de `@Timeout` es `-1`, que significa "no configurado, heredar de la capa superior de la cadena de prioridad"
- Establecer a `0` significa "sin timeout"
- Establecer a un número positivo significa los milisegundos específicos de timeout
- `-1` es un valor fuera del dominio legal de timeout de OkHttp (OkHttp solo acepta 0 y números positivos), usado como marcador de "no configurado" no conflictará con valores de timeout legales

## Cuatro dimensiones de timeout

| Propiedad | Significado | Valor predeterminado |
|------|------|--------|
| `connectTimeoutMs` | Timeout de conexión (milisegundos) | `-1` (heredar de la capa superior) |
| `readTimeoutMs` | Timeout de lectura (milisegundos) | `-1` (heredar de la capa superior) |
| `writeTimeoutMs` | Timeout de escritura (milisegundos) | `-1` (heredar de la capa superior) |
| `callTimeoutMs` | Timeout de llamada completa (milisegundos) | `-1` (heredar de la capa superior) |

## @Timeout de clase

Declarar `@Timeout` en la interfaz para establecer timeout para todos los métodos de dicha interfaz:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## @Timeout de método

Declarar `@Timeout` en un método específico para sobrescribir la configuración de timeout de clase o global:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Hereda readTimeoutMs = 5000 de la clase
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Sobrescritura por método: la interfaz de consulta lenta usa un timeout más largo
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## @Timeout solo en método (sin anotación de clase)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Usa la configuración global de timeout
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Solo este método usa timeout personalizado
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

[Anterior: OkHttpClient personalizado](okhttp-client.md) | [Siguiente: Log](logging.md)