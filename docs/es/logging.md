# Registro de logs
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | **Español** | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

El componente soporta registro de logs global y declarativo.

## Registro de logs global

El registro de logs global esta deshabilitado por defecto (`enable=false`), se debe habilitar activamente. Al habilitarlo, por defecto usa la estrategia `BASIC` que solo imprime la linea de solicitud/respuesta (incluyendo codigo de estado y duracion), con overhead insignificante. La configuracion por defecto es:

```yaml
retrofit:
  global-log:
    # Habilitar registro de logs (por defecto false)
    enable: false
    # Nivel global de registro de logs
    log-level: info
    # Estrategia global de registro de logs (por defecto BASIC, solo imprime linea de solicitud/respuesta)
    log-strategy: basic
    # Si agregar logs de solicitud
    aggregate: true
    # Nombre del log, por defecto es el nombre completo de la clase LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Headers de solicitud sensibles que deben ocultarse en los logs
    # Por defecto se ocultan: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Nota: la configuracion del usuario sobrescribirá completamente el valor por defecto, se deben incluir los elementos que se desea mantener ocultos
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

Las cuatro estrategias de registro de logs significan:

1. **NONE**: no imprimir logs
2. **BASIC**: solo imprimir la linea de solicitud y respuesta
3. **HEADERS**: imprimir la linea de solicitud y respuesta, y los headers de solicitud/respuesta
4. **BODY**: imprimir la linea de solicitud y respuesta, los headers de solicitud/respuesta, y el cuerpo de solicitud/respuesta (si existe)

## Registro de logs declarativo

Si solo se necesita imprimir logs para algunas solicitudes, se puede usar la anotacion `@Logging` en las interfaces o metodos relevantes.

## Extension personalizada

Si se necesita modificar el comportamiento del registro de logs, se puede heredar `LoggingInterceptor` y configurarlo como un Spring Bean.

---

[Anterior: Configuracion de timeout a nivel de metodo](timeout.md) | [Siguiente: Reintento de solicitudes](retry.md)