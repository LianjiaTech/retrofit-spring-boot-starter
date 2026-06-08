# 자주 묻는 질문
[English](../en/faq.md) | [简体中文](../cn/faq.md) | [繁體中文](../tw/faq.md) | [日本語](../ja/faq.md) | **한국어** | [Español](../es/faq.md) | [Türkçe](../tr/faq.md) | [Русский](../ru/faq.md)

## RetrofitAutoConfiguration이 자동으로 로드되지 않음

특정 시나리오下(예: `@SpringBootApplication(exclude = ...)` 사용 또는 XML 설정과混合된 프로젝트), `RetrofitAutoConfiguration`이 정상적으로 로드되지 않을 수 있습니다. 이 경우 수동으로 구성하여 가져올 수 있습니다:

```java
@Configuration
@ImportAutoConfiguration({RetrofitAutoConfiguration.class})
public class SpringBootAutoConfigBridge {
}
```

프로젝트가 여전히 Spring XML 설정 파일을 사용하는 경우, XML 설정 파일에 Spring Boot 자동 설정 클래스를 추가必须합니다:

```xml
<!-- Spring Boot 자동 설정 클래스 가져오기 -->
<bean class="com.yourpackage.config.SpringBootAutoConfig"/>
```

## Spring Boot 설정 파일이 적용되지 않음

`application.yml` 또는 `application.properties`의 설정이 적용되지 않는 경우, `RetrofitProperties` Bean을 수동으로 구성할 수 있습니다:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties 각 설정 값을 수동으로 수정
    return retrofitProperties;
}
```

## 속성명 path-math와 클래스명 PathMatchInterceptor의 차이

설정 속성 `auto-set-prototype-scope-for-path-math-interceptor`의 `path-math`는 과거 명명이며, 해당 인터셉터 클래스명은 `PathMatchInterceptor`( `match` 사용)입니다. 이는已知된 과거 명명差异이며, 기능 사용에 영향을 주지 않습니다.

## RetrofitClient 스캔 경로 수동 지정

기본값으로 컴포넌트는 Spring Boot 스캔 경로를 사용하여 `RetrofitClient`를 등록합니다. 스캔 경로를 수동으로 지정해야 하는 경우, 설정 클래스에 `@RetrofitScan` 어노테이션을 추가할 수 있습니다.

## Jackson 직렬화 설정 수정

Jackson의 직렬화/역직렬화 동작을 커스텀해야 하는 경우, `JacksonConverterFactory`의 Spring Bean 설정을 직접 오버라이드하면 됩니다. 컴포넌트는 기본값으로 `retrofit2.converter.jackson.JacksonConverterFactory`를 사용하며, Bean으로 등록한 후 커스텀된 Jackson `ObjectMapper` 설정이 자동으로 적용됩니다.

---

[기능 목록으로 돌아가기](../../README.md)