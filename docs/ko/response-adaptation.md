# HTTP 응답 결과 자동 어댑터
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | **한국어** | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

이 컴포넌트는 HTTP 응답 결과를 Java 인터페이스에 정의된 반환 타입으로 자동 어댑터합니다. 현재 다음 반환 타입을 지원합니다:

- `Call<T>`: 어댑터 처리를 실행하지 않고, `Call<T>` 객체를 직접 반환
- `String`: Response Body를 `String`으로 어댑터하여 반환
  - 기본값으로 JSON Converter를 사용하여 Response Body의 bytes를 String으로 변환; Response Body를 String으로 직접 얻으려면, `Converter.Factory`를 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`로 지정
- 기본 타입(`Long`/`Integer`/`Boolean`/`Float`/`Double`): Response Body를 해당 기본 타입으로 어댑터
- `CompletableFuture<T>`: Response Body를 `CompletableFuture<T>` 객체로 어댑터하여 반환
- `Void`: 반환 타입을 무시할 때 사용
- `Response<T>`: 응답을 `Retrofit.Response<T>` 객체로 어댑터하여 반환
- `Mono<T>`: Project Reactor 반응형 반환 타입
- `Single<T>`: RxJava 반응형 반환 타입(RxJava2/RxJava3 지원)
- `Completable`: RxJava 반응형 반환 타입, HTTP 요청에 응답 본문이 없는 시나리오에서 사용(RxJava2/RxJava3 지원)
- 임의 POJO 타입: Response Body를 해당 POJO 객체로 어댑터하여 반환

## 어댑터 구현 방식

Retrofit은 `CallAdapterFactory`를 통해 `Call<T>` 객체를 인터페이스 메서드의 반환값 타입으로 어댑터합니다. 이 컴포넌트는 다음 `CallAdapterFactory` 구현을 확장합니다:

- **BodyCallAdapterFactory**
  - HTTP 요청을 동기 실행하고, 응답 본문 내용을 메서드의 반환값 타입으로 어댑터
  - 임의 메서드 반환값 타입에 사용 가능, 우선순위가 가장 낮음

- **ResponseCallAdapterFactory**
  - HTTP 요청을 동기 실행하고, 응답 본문 내용을 `Retrofit.Response<T>`로 어댑터하여 반환
  - 메서드 반환값 타입이 `Retrofit.Response<T>`인 경우만 적용

- **반응형 프로그래밍 관련 CallAdapterFactory**
  - `Mono<T>`, `Single<T>`, `Completable` 등 반응형 타입 지원

`CallAdapter.Factory`를 상속하면 HTTP 응답에서 Java 인터페이스 반환 타입으로의 어댑터를 어떤 방식으로든 구현할 수 있습니다. 컴포넌트는 `retrofit.global-call-adapter-factories` 설정으로 전역 호출 어댑터 팩토리를 구성할 수 있습니다:

```yaml
retrofit:
  # 전역 어댑터 팩토리(컴포넌트가 확장한 CallAdapterFactory는 이미 내장되어 있으므로 중복 설정하지 마세요)
  global-call-adapter-factories:
    # ...
```

각 Java 인터페이스마다 `@RetrofitClient.callAdapterFactories`를 통해 해당 인터페이스에서 사용하는 `CallAdapter.Factory`를 지정할 수 있습니다.

---

[기능 목록으로 돌아가기](../README.md) | [다음: 커스텀 데이터 변환기](converter.md)