# 오류 디코더
[English](../en/error-decoder.md) | [简体中文](../cn/error-decoder.md) | [繁體中文](../tw/error-decoder.md) | [日本語](../ja/error-decoder.md) | **한국어** | [Español](../es/error-decoder.md) | [Türkçe](../tr/error-decoder.md) | [Русский](../ru/error-decoder.md)

HTTP 요청 오류(예외 발생 또는 응답 데이터가 예상과不一致)가 발생할 때, 오류 디코더는 HTTP 관련 정보를 커스텀 예외로 디코딩할 수 있습니다.

## 사용 방식

`@RetrofitClient` 어노테이션의 `errorDecoder()` 속성으로 해당 인터페이스의 오류 디코더를 지정합니다. 커스텀 오류 디코더는 `ErrorDecoder` 인터페이스를 구현必须합니다.

## ErrorDecoder 비활성화

`retrofit.enable-error-decoder=false` 설정으로 ErrorDecoder 기능을 비활성화할 수 있습니다.

---

[이전: 서킷브레이커/폴백](degrade.md) | [다음: 마이크로서비스간 HTTP 호출](microservice.md)