# 자주 묻는 질문
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | **한국어** | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration이 자동 로드되지 않음

일부 시나리오(`@SpringBootApplication(exclude = ...)` 사용 또는 XML 설정과混用하는 프로젝트 등)에서 `RetrofitAutoConfiguration`이正常으로 로드되지 않을 수 있습니다. 이 경우 수동으로 설정을 가져올 수 있습니다:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

프로젝트가 Spring XML 설정 파일을 여전히 사용하는 경우, XML 설정 파일에 Spring Boot 자동 설정 클래스를 추가해야 합니다:

```xml
<!-- Spring Boot 자동 설정 클래스 가져오기 -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 설정 파일이 유효하지 않음

`application.yml` 또는 `application.properties`의 설정이 유효하지 않은 경우, `RetrofitProperties` Bean을 수동으로 설정할 수 있습니다:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties의 각 설정 값을 수동으로 수정
    return retrofitProperties;
}
```

## 프로퍼티명 path-math와 클래스명 PathMatchInterceptor의 차이

설정 프로퍼티 `auto-set-prototype-scope-for-path-math-interceptor`의 `path-math`는歴史命名이며, 해당 인터셉터 클래스명은 `PathMatchInterceptor`(`match` 사용)입니다. 이는 알려진歴史命名 차이이며, 기능 사용에 영향이 없습니다.

## RetrofitClient 스캔 경로 수동 지정

기본적으로 컴포넌트는 Spring Boot 스캔 경로를 사용하여 `RetrofitClient`를 등록합니다. 스캔 경로를 수동으로 지정해야 하는 경우, 설정 클래스에 `@RetrofitScan` 어노테이션을 추가하세요.

## Jackson serialization 설정 수정

Jackson의 serialization/deserialization 동작을 커스터마이징해야 하는 경우, `JacksonConverterFactory`의 Spring Bean 설정을 직접 오버라이드하세요. 컴포넌트는 기본적으로 `retrofit2.converter.jackson.JacksonConverterFactory`를 사용하며, 이를 Bean으로 등록하면 커스텀 Jackson `ObjectMapper` 설정이 자동으로 적용됩니다.

---

[기능 특성 목차로 돌아가기](../ko/README.md)