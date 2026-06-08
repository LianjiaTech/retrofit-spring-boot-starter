# 데이터 컨버터 커스터마이징
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | **한국어** | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit은 `Converter`를 사용하여 `@Body` 어노테이션의 객체를 HTTP 요청 본체로 변환하고, HTTP 응답 본체를 Java 객체로 변환합니다. 다음 Converter를 지원합니다:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- Jackson 3: com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## 글로벌 설정

컴포넌트는 `retrofit.global-converter-factories` 설정으로 글로벌 `Converter.Factory`를 지원합니다. 기본값은 `retrofit2.converter.jackson.JacksonConverterFactory`입니다:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

Jackson 설정을 수정하려면, `JacksonConverterFactory`의 Bean 설정을 직접 오버라이드하세요.

## 인터페이스 수준 설정

각 Java 인터페이스에서 `@RetrofitClient.converterFactories`로 현재 인터페이스에 사용하는 `Converter.Factory`를 지정할 수 있습니다.

## 원시 문자열 결과

인터페이스의 원시 결과가 String 텍스트이고 JSON 컨버터로 변환할 수 없는 경우, `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`를 사용할 수 있습니다. 이 컨버터는 결과를 직접 String으로 반환합니다.

---

[이전 섹션: HTTP 응답 결과 자동 적응](response-adaptation.md) | [다음 섹션: OkHttpClient 및 Call.Factory SPI 커스터마이징](okhttp-client.md)