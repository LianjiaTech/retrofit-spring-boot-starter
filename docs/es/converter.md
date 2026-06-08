# Convertidor de datos personalizado
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | **Español** | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit usa `Converter` para convertir objetos anotados con `@Body` en cuerpos de solicitud HTTP, y cuerpos de respuesta HTTP en objetos Java. Soporta los siguientes Converters:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- Jackson 3: com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## Configuracion global

El componente soporta la configuracion global de `Converter.Factory` mediante `retrofit.global-converter-factories`, por defecto `retrofit2.converter.jackson.JacksonConverterFactory`:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

Si se necesita modificar la configuracion de Jackson, basta con sobrescribir la configuracion del bean de `JacksonConverterFactory`.

## Configuracion a nivel de interfaz

Para cada interfaz Java, se puede especificar el `Converter.Factory` utilizado por la interfaz actual mediante `@RetrofitClient.converterFactories`.

## Resultado de cadena original

Si el resultado original devuelto por la interfaz es texto String y no puede ser convertido por el convertidor JSON, se puede usar `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`, que convierte directamente el resultado a String.

---

[Anterior: Adaptacion automatica de resultados de respuesta HTTP](response-adaptation.md) | [Siguiente: OkHttpClient personalizado y Call.Factory SPI](okhttp-client.md)