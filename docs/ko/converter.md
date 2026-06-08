# 커스텀 데이터 변환기
[English](../en/converter.md) | [简体中文](../cn/converter.md) | [繁體中文](../tw/converter.md) | [日本語](../ja/converter.md) | **한국어** | [Español](../es/converter.md) | [Türkçe](../tr/converter.md) | [Русский](../ru/converter.md)

Retrofit은 `Converter`를 사용하여 `@Body` 어노테이션의 객체를 HTTP 요청 본문으로 변환하고, HTTP 응답 본문을 Java 객체로 변환합니다. 다음 Converter를 지원합니다:

- [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
- [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
- [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
- [Protobuf](https://developers.google.com/protocol-buffers/): com.squareup.retrofit2:converter-protobuf
- [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
- [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
- FastJson: com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory

## 전역 설정

컴포넌트는 `retrofit.global-converter-factories` 설정으로 전역 `Converter.Factory`를 구성할 수 있습니다. 기본값은 `retrofit2.converter.jackson.JacksonConverterFactory`입니다:

```yaml
retrofit:
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
```

## 인터페이스급 설정

각 Java 인터페이스마다 `@RetrofitClient.converterFactories`를 통해 해당 인터페이스에서 사용하는 `Converter.Factory`를 지정할 수 있습니다.

## 원시 문자열 결과

인터페이스의 원시 결과가 String 텍스트이고 JSON 변환기로 변환할 수 없는 경우, `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`를 사용할 수 있습니다. 이 변환기는 결과를 String으로 직접 변환하여 반환합니다.

---

[이전: HTTP 응답 결과 자동 어댑터](response-adaptation.md) | [다음: 커스텀 OkHttpClient](okhttp-client.md)