# HTTP 응답 결과 자동 적응
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | **한국어** | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | [Русский](../ru/response-adaptation.md)

이 컴포넌트는 HTTP 응답 결과를 Java 인터페이스에 정의된 반환 타입으로 자동 적응합니다. 현재 다음 반환 타입을 지원합니다:

- `Call<T>`: 적응 처리를 수행하지 않고 `Call<T>` 객체를 직접 반환
- `String`: Response Body를 `String`으로 적응하여 반환
  - 기본적으로 JSON Converter를 사용하여 Response Body의 bytes를 String으로 변환합니다. Response Body를 직접 String으로 받으려면 `Converter.Factory`로 `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`를 지정하세요
- 기본 타입 (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Response Body를 해당 기본 타입으로 적응
- `CompletableFuture<T>`: Response Body를 `CompletableFuture<T>` 객체로 적응하여 반환
- `Void`: 반환 타입에 관심이 없을 때 사용
- `Response<T>`: 응답을 `Retrofit.Response<T>` 객체로 적응하여 반환
- `Mono<T>`: Project Reactor 리액티브 반환 타입
- `Single<T>`: RxJava 리액티브 반환 타입 (RxJava2/RxJava3 지원)
- `Completable`: RxJava 리액티브 반환 타입. HTTP 요청에 응답 본체가 없는 경우에 사용 (RxJava2/RxJava3 지원)
- 임의의 POJO 타입: Response Body를 해당 POJO 객체로 적응하여 반환

## 적응 구현 방식

Retrofit은 하위 레벨에서 `CallAdapterFactory`를 사용하여 `Call<T>` 객체를 인터페이스 메서드의 반환 값 타입으로 적응합니다. 이 컴포넌트는 다음 `CallAdapterFactory` 구현을 확장합니다:

- **BodyCallAdapterFactory**
  - HTTP 요청을 동기적으로 실행하고, 응답 본체 내용을 메서드의 반환 값 타입으로 적응
  - 임의의 메서드 반환 값 타입에 사용 가능. 최저 우선순위

- **ResponseCallAdapterFactory**
  - HTTP 요청을 동기적으로 실행하고, 응답 본체 내용을 `Retrofit.Response<T>`로 적응하여 반환
  - 메서드 반환 값 타입이 `Retrofit.Response<T>`인 경우만 유효

- **리액티브 프로그래밍 관련 CallAdapterFactory**
  - `Mono<T>`, `Single<T>`, `Completable` 등 리액티브 타입 지원

`CallAdapter.Factory`를 상속하여, 임의의 방식으로 HTTP 응답을 Java 인터페이스 반환 타입에 적응할 수 있습니다. 컴포넌트는 `retrofit.global-call-adapter-factories` 설정으로 글로벌 호출 적응 팩토리를 지원합니다:

```yaml
retrofit:
  # 글로벌 적응 팩토리 (컴포넌트 확장 CallAdapterFactory는 내장됨, 중복 설정하지 마세요)
  global-call-adapter-factories:
    # ...
```

각 Java 인터페이스에서 `@RetrofitClient.callAdapterFactories`로 현재 인터페이스에 사용하는 `CallAdapter.Factory`를 지정할 수도 있습니다.

---

[기능 특성 목차로 돌아가기](../ko/README.md) | [다음 섹션: 데이터 컨버터 커스터마이징](converter.md)