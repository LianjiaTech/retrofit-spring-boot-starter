# Log
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | [한국어](../ko/logging.md) | **Español** | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

El componente admite log global y log declarativo.

## Log global

El log global está deshabilitado por defecto (`enable=false`), necesita ser activado manualmente. Al activarse, por defecto usa la estrategia `BASIC` para imprimir solo la línea de petición/respuesta (incluyendo código de estado y duración), con un overhead insignificante. La configuración predeterminada es la siguiente:

```yaml
retrofit:
  global-log:
    # Habilitar log (predeterminado false)
    enable: false
    # Nivel de log global
    log-level: info
    # Estrategia de log global (predeterminado BASIC, solo imprime la línea de petición/respuesta)
    log-strategy: basic
    # Si agregar logs de petición
    aggregate: true
    # Nombre del log, predeterminado es el nombre completo de la clase LoggingInterceptor
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # Headers de petición sensibles que deben ocultarse en el log
    # Ocultados por defecto: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # Nota: la configuración del usuario sobrescribirá completamente los valores predeterminados, debe incluir los items que aún desea ocultar
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

Los cuatro tipos de estrategias de log tienen los siguientes significados:

1. **NONE**: no imprimir log
2. **BASIC**: solo imprime la línea de petición y respuesta
3. **HEADERS**: imprime la línea de petición y respuesta junto con los headers de petición/respuesta
4. **BODY**: imprime la línea de petición y respuesta, headers de petición/respuesta y body de petición/respuesta (si existe)

## Log declarativo

Si solo se necesita imprimir log para algunas peticiones, se puede usar la anotación `@Logging` en las interfaces o métodos correspondientes.

## Extensión personalizada

Si se necesita modificar el comportamiento de impresión de log, se puede heredar `LoggingInterceptor` y configurarlo como un bean de Spring.

---

[Anterior: Configuración de timeout por método](timeout.md) | [Siguiente: Reintento de petición](retry.md)