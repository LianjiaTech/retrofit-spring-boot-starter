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

## 기본 Scalars Converter (String / 기본 타입)

컴포넌트는 기본적으로 최우선(맨 앞)에 `ScalarsConverterFactory`(`com.squareup.retrofit2:converter-scalars` 제공)를 내장하며, `String` 및 기본 타입(`Integer`/`Long`/`Boolean`/`Double` 등) 반환값을 응답 본문의 원시 텍스트로 반환합니다(JSON Converter를 거치지 않음). POJO는 여전히 `global-converter-factories`에 설정된 컨버터(기본 Jackson)로 역직렬화됩니다.

이 기본 동작은 설정으로 비활성화할 수 있습니다(비활성화하면 `String` 반환값은 JSON Converter 역직렬화로 돌아갑니다):

```yaml
retrofit:
  enable-scalars-converter: false
```

> 이전 버전의 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`는 제거되었으며, 그 역할은 기본 내장된 `ScalarsConverterFactory`가 대체합니다. `@RetrofitClient.converterFactories`나 전역 설정에서 이 클래스를 명시적으로 참조했다면 해당 참조를 제거하세요.
---

[이전: HTTP 응답 결과 자동 어댑터](response-adaptation.md) | [다음: 커스텀 OkHttpClient](okhttp-client.md)