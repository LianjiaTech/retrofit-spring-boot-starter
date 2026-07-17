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

## Scalars Converter por defecto (String / tipos basicos)

Por defecto el starter registra `ScalarsConverterFactory` (de `com.squareup.retrofit2:converter-scalars`) como primer convertidor, por lo que los valores de retorno `String` y de tipos basicos (`Integer`/`Long`/`Boolean`/`Double`, etc.) se devuelven como el texto original del cuerpo de respuesta, sin pasar por un convertidor JSON; los POJO siguen deserializandose con el convertidor configurado en `global-converter-factories` (Jackson por defecto).

Puedes desactivar este comportamiento por defecto mediante configuracion (al desactivarlo, los valores de retorno `String` vuelven a deserializarse mediante un convertidor JSON):

```yaml
retrofit:
  enable-scalars-converter: false
```

> La clase `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory` de versiones anteriores se ha eliminado; su rol lo asume el `ScalarsConverterFactory` integrado por defecto. Si referenciabas esta clase explicitamente en `@RetrofitClient.converterFactories` o en la configuracion global, elimina dicha referencia.

---

[Anterior: Adaptacion automatica de resultados de respuesta HTTP](response-adaptation.md) | [Siguiente: OkHttpClient personalizado y Call.Factory SPI](okhttp-client.md)