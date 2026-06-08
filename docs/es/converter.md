# Convertidor de datos personalizado
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | [한국어](../ko/converter.md) | **Español** | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit usa `Converter` para convertir objetos anotados con `@Body` en cuerpo de petición HTTP, y para convertir el cuerpo de respuesta HTTP en objetos Java. Se admiten los siguientes Converter:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## Configuración global

El componente admite la configuración de `Converter.Factory` global mediante `retrofit.global-converter-factories`, con valor predeterminado `retrofit2.converter.jackson.JacksonConverterFactory`:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

## Configuración por interfaz

Para cada interfaz Java, se puede especificar el `Converter.Factory` utilizado por la interfaz actual mediante `@RetrofitClient.converterFactories`.

## Resultado de cadena original

Si el resultado original devuelto por la interfaz es texto String y no se puede convertir con el convertidor JSON, se puede usar `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`, que convierte directamente el resultado a String.

---

[Anterior: Adaptación automática de resultados de respuesta HTTP](response-adaptation.md) | [Siguiente: OkHttpClient personalizado](okhttp-client.md)