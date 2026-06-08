# GraalVM Native Image / AOT 지원
[English](../en/aot.md) | [简体中文](../cn/aot.md) | [繁體中文](../tw/aot.md) | [日本語](../ja/aot.md) | **한국어** | [Español](../es/aot.md) | [Türkçe](../tr/aot.md) | [Русский](../ru/aot.md)

컴포넌트는 Spring AOT 지원을 내장하고 있으며, Spring Boot 3.x / 4.x에서 GraalVM Native Image로 컴파일할 때 **즉시 사용 가능. `reflect-config.json` / `proxy-config.json`을手書き할 필요가 없습니다**.

빌드 시간(`spring-boot:process-aot` 또는 native 컴파일)에서 각 `@RetrofitClient` 인터페이스에 대해 다음이 자동 등록됩니다:

- **JDK 동적 프록시**: `Retrofit.create(인터페이스)`와 서킷브레이커/데그레이드 프록시는 인터페이스 기반으로 JDK 프록시를 생성;
- **인터페이스 리플렉션**: 메서드 시그니처와 매개변수 어노테이션은 native에서 리플렉션可視이어야 하며, Retrofit이 HTTP 요청을 분석하기 위해使用;
- **어노테이션 참조 클래스의 리플렉션 구축**: `@RetrofitClient`의 `baseUrlParser` / `converterFactories` / `callAdapterFactories` / `errorDecoder` / `fallback` / `fallbackFactory`, 및 `@InterceptMark`(`@Intercept` / `@Sign` 포함) 어노테이션의 `handler` 인터셉터 클래스. 런타임에서 리플렉션으로 생성 및 속성 주입될 수 있습니다;
- **Actuator 값 객체 serialization**: `/actuator/retrofit` 응답 결과의 리플렉션 serialization.

> 이 기능은 `RetrofitAotProcessor`(`BeanFactoryInitializationAotProcessor`)로 구현되어 있습니다. **AOT 빌드 시간만 유효**하며, 일반 JVM 시작과 native 런타임에서는 로직이 실행되지 않고, 기능과 성능에 영향이 없습니다.
>
> 커스텀 `Converter.Factory` / `CallAdapter.Factory` / `ErrorDecoder` 등이 JSON serialization으로 복잡한 비즈니스 엔티티가 되는 경우, 비즈니스 엔티티 자체의 native 리플렉션 hints는 Spring의 표준 방법(예: `@RegisterReflectionForBinding`)으로 선언해야 합니다 -- 이는具体적인 비즈니스 모델과 관련되며, 컴포넌트의 책임 범위外입니다.

---

[이전 섹션: Actuator Endpoint](actuator.md) | [다음 섹션: 마이크로서비스 간 HTTP 호출](microservice.md)