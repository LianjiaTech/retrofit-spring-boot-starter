# retrofit-spring-boot-starter

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | **한국어** | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-17+-4EB1BA.svg)](https://docs.oracle.com/en/java/javase/17/)
[![License](https://img.shields.io/badge/SpringBoot-3+-green.svg)](https://docs.spring.io/spring-boot/)

**[retrofit](https://square.github.io/retrofit/)은 HTTP API를 Java 인터페이스로 변환하는 라이브러리입니다. 이 컴포넌트는 Retrofit과 SpringBoot를 깊이 통합하고, 여러 가지 실용적인 기능 향상을 지원합니다.**

- **Spring Boot 3.x/4.x 프로젝트는 retrofit-spring-boot-starter 4.x를 사용하세요**
    - Spring Boot 4.x는 기본적으로 jackson3을 사용하지만, 이 컴포넌트의 기본 converter는 jackson2입니다. **Spring Boot 4.x 프로젝트에서는 글로벌 converter를 jackson3으로 설정하는 것을 권장합니다**
    - 설정 방법: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x 프로젝트는 [retrofit-spring-boot-starter 2.x](https://github.com/LianjiaTech/retrofit-spring-boot-starter/tree/2.x)를 사용하세요**. Spring Boot 1.4.2 이상 버전을 지원합니다.

## 빠른 시작

### 의존성 추가

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

대부분의 Spring Boot 프로젝트에서는 의존성을 추가하면 사용할 수 있습니다.

### HTTP Java 인터페이스 정의

**인터페이스에 `@RetrofitClient` 어노테이션을 반드시 표시해야 합니다!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

   /**
    * ID로 사용자 이름 조회
    */
   @POST("getName")
   String getName(@Query("id") Long id);
}
```

> 주의: **메서드 요청 경로의 시작에 `/`를 사용할 때는 주의가 필요합니다**. Retrofit에서 `baseUrl=http://localhost:8080/api/test/`인 경우, 메서드 요청 경로가 `person`이면 전체 요청 경로는 `http://localhost:8080/api/test/person`입니다. 메서드 요청 경로가 `/person`이면 전체 요청 경로는 `http://localhost:8080/person`입니다.

### 주입 및 사용

**인터페이스를 다른 Service에 주입하여 사용할 수 있습니다!**

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
       // call userService
    }
}
```

## 기능 특성

- [HTTP 응답 결과 자동 적응](response-adaptation.md)
- [데이터 컨버터 커스터마이징](converter.md)
- [OkHttpClient 및 Call.Factory SPI 커스터마이징](okhttp-client.md)
- [메서드 수준 타임아웃 설정](timeout.md)
- [로그 출력](logging.md)
- [요청 재시도](retry.md)
- [인터셉터](interceptor.md)
- [서킷브레이커 / 데그레이드](degrade.md)
- [에러 디코더](error-decoder.md)
- [메트릭 모니터링 (Micrometer)](metrics.md)
- [Actuator Endpoint](actuator.md)
- [GraalVM Native Image / AOT 지원](aot.md)
- [마이크로서비스 간 HTTP 호출](microservice.md)
- [RetrofitClient 어노테이션 커스터마이징](custom-annotation.md)
- [전체 설정 항목 레퍼런스](configuration.md)
- [기타 기능 예제](examples.md)
- [자주 묻는 질문](faq.md)