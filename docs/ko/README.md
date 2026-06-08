# retrofit-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![Maven Central](https://img.shields.io/maven-central/v/com.github.lianjiatech/retrofit-spring-boot-starter.svg?label=Maven)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-1.4.2+-green.svg)](https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LianjiaTech/retrofit-spring-boot-starter)

[English](../en/README.md) | [简体中文](../../README.md) | [繁體中文](../tw/README.md) | [日本語](../ja/README.md) | **한국어** | [Español](../es/README.md) | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

**[Retrofit](https://square.github.io/retrofit/)은 HTTP API를 Java 인터페이스로 정의할 수 있게 해줍니다. 이 컴포넌트는 Retrofit과 Spring Boot를 깊이 있게 통합하고, 여러 실용적인 기능 향상을 지원합니다.**

- **Spring Boot 3.x/4.x 프로젝트**는 retrofit-spring-boot-starter **4.x**를 사용하세요
  - Spring Boot 4.x는 기본적으로 Jackson 3을 사용하며, 이 컴포넌트의 기본 Converter는 Jackson 2를 사용하므로 **4.x 프로젝트에서는 전역 Converter를 Jackson 3으로 설정하는 것을 권장합니다**
  - 설정 방법: `retrofit.global-converter-factories=com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory`
- **Spring Boot 1.x/2.x 프로젝트**는 retrofit-spring-boot-starter **2.x**를 사용하세요, Spring Boot 1.4.2 이상 버전을 지원합니다

> 프로젝트는 지속적으로 최적화 및 업데이트되고 있습니다. ISSUE와 PR을 환영합니다! star를 주시는 것은 우리가 지속적으로 업데이트하는 가장 큰 지원입니다!

GitHub: [https://github.com/LianjiaTech/retrofit-spring-boot-starter](https://github.com/LianjiaTech/retrofit-spring-boot-starter)
Gitee: [https://gitee.com/lianjiatech/retrofit-spring-boot-starter](https://gitee.com/lianjiatech/retrofit-spring-boot-starter)

## 빠른 시작

### 의존성 추가

```xml
<dependency>
    <groupId>com.github.lianjiatech</groupId>
    <artifactId>retrofit-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

의존성을 추가한 후 바로 사용할 수 있습니다. 문제가 발생하면 [자주 묻는 질문](faq.md)을 참조하세요.

### HTTP 인터페이스 정의

**인터페이스는 `@RetrofitClient` 어노테이션으로 반드시 표시해야 합니다!**

```java
@RetrofitClient(baseUrl = "http://localhost:8080/api/user/")
public interface UserService {

    /**
     * id로 사용자 이름 조회
     */
    @POST("getName")
    String getName(@Query("id") Long id);
}
```

> 주의: **메서드 요청 경로에 `/`로 시작하는 것을慎重히 사용하세요**. Retrofit의 경로 조합 규칙: `baseUrl = http://localhost:8080/api/test/`인 경우, 메서드 경로 `person`의 전체 경로는 `http://localhost:8080/api/test/person`이 되고, 메서드 경로 `/person`의 전체 경로는 `http://localhost:8080/person`이 됩니다.

### 주입 및 사용

인터페이스를 다른 Service에 주입하여 사용할 수 있습니다:

```java
@Service
public class BusinessService {

    @Autowired
    private UserService userService;

    public void doBusiness() {
        // userService 호출
    }
}
```

### HTTP 요청 어노테이션

HTTP 요청 관련 어노테이션은 모두 Retrofit 원본 어노테이션을 사용합니다:

| 어노테이션 분류 | 지원 어노테이션 |
|----------|-----------|
| 요청 방식 | `@GET` `@HEAD` `@POST` `@PUT` `@DELETE` `@OPTIONS` `@HTTP` |
| 요청 헤더 | `@Header` `@HeaderMap` `@Headers` |
| Query 파라미터 | `@Query` `@QueryMap` `@QueryName` |
| Path 파라미터 | `@Path` |
| Form 파라미터 | `@Field` `@FieldMap` `@FormUrlEncoded` |
| 요청 본문 | `@Body` |
| 파일 업로드 | `@Multipart` `@Part` `@PartMap` |
| URL 파라미터 | `@Url` |

> 자세한 정보는 [Retrofit 공식 문서](https://square.github.io/retrofit/)를 참조하세요

## 기능 특성

- [x] [HTTP 응답 결과 자동 어댑터](response-adaptation.md)
- [x] [커스텀 데이터 변환기](converter.md)
- [x] [커스텀 OkHttpClient와 Call.Factory SPI](okhttp-client.md)
- [x] [메서드 수준 타임아웃 설정](timeout.md)
- [x] [로그 출력](logging.md)
- [x] [요청 재시도](retry.md)
- [x] [인터셉터](interceptor.md)
- [x] [서킷브레이커/폴백](degrade.md)
- [x] [오류 디코더](error-decoder.md)
- [x] [마이크로서비스간 HTTP 호출](microservice.md)
- [x] [커스텀 RetrofitClient 어노테이션](custom-annotation.md)
- [x] [전체 설정 항목 참조](configuration.md)
- [x] [기타 기능 예시](examples.md)
- [x] [자주 묻는 질문](faq.md)

## 피드백

문제가 있으면 issue를 제출하거나 QQ 그룹에 참여하여 피드백해 주세요.

그룹 번호: 806714302

![QQ群图片](https://github.com/LianjiaTech/retrofit-spring-boot-starter/blob/master/group.png)