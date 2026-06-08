# Configuracion de timeout a nivel de metodo
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | [한국어](../ko/timeout.md) | **Español** | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

El componente soporta la configuracion de parametros de timeout a nivel de metodo o clase mediante la anotacion `@Timeout`, sobrescribiendo la configuracion global de timeout.

## Cadena de prioridad

```
Metodo @Timeout → Clase @Timeout → Configuracion global (GlobalTimeoutProperty)
```

- El valor por defecto de los atributos de `@Timeout` es `-1`, lo que significa "no configurado, heredar de la capa superior en la cadena de prioridad"
- Establecer a `0` significa "sin timeout"
- Establecer a un numero positivo indica el timeout en milisegundos
- `-1` es un valor ilegal para timeouts de OkHttp (OkHttp solo acepta 0 y numeros positivos), usado como marcador de "no configurado" y no conflige con valores de timeout validos

## Cuatro dimensiones de timeout

| Atributo | Descripcion | Valor por defecto |
|------|------|--------|
| `connectTimeoutMs` | Timeout de conexion (milisegundos) | `-1` (heredar de la capa superior) |
| `readTimeoutMs` | Timeout de lectura (milisegundos) | `-1` (heredar de la capa superior) |
| `writeTimeoutMs` | Timeout de escritura (milisegundos) | `-1` (heredar de la capa superior) |
| `callTimeoutMs` | Timeout de llamada completa (milisegundos) | `-1` (heredar de la capa superior) |

## @Timeout a nivel de clase

Declarar `@Timeout` en la interfaz para establecer el timeout de todos los metodos de esa interfaz:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## @Timeout a nivel de metodo

Declarar `@Timeout` en un metodo especifico para sobrescribir la configuracion de timeout a nivel de clase o global:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Hereda readTimeoutMs = 5000 a nivel de clase
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Sobrescritura a nivel de metodo: consulta lenta usa timeout mas largo
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## @Timeout solo a nivel de metodo (sin anotacion a nivel de clase)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // Usa la configuracion global de timeout
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // Solo este metodo usa timeout personalizado
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## Mecanismo de implementacion

- `@Timeout` a nivel de clase: se procesa durante la creacion de OkHttpClient, sin overhead en tiempo de ejecucion
- `@Timeout` a nivel de metodo: `TimeoutCallFactory` pre-crea clones de OkHttpClient por metodo, busca en tiempo de ejecucion mediante Invocation tag, interfaces sin anotacion tienen overhead adicional nulo

---

[Anterior: OkHttpClient personalizado](okhttp-client.md) | [Siguiente: Registro de logs](logging.md)