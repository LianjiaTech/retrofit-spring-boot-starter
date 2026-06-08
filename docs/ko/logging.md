# 로그 출력
[English](../en/logging.md) | [简体中文](../cn/logging.md) | [繁體中文](../tw/logging.md) | [日本語](../ja/logging.md) | **한국어** | [Español](../es/logging.md) | [Türkçe](../tr/logging.md) | [Русский](../ru/logging.md)

컴포넌트는 글로벌 로그 출력과 선언형 로그 출력을 지원합니다.

## 글로벌 로그 출력

글로벌 로그 출력은 기본적으로 비활성화(`enable=false`)되어 있습니다. 수동으로 활성화해야 합니다. 활성화 후 기본적으로 `BASIC` 전략으로 요청/응답 행(상태 코드와 소요 시간 포함)만 출력하며, 오버헤드는 무시할 수 있는 수준입니다. 기본 설정은 다음과 같습니다:

```yaml
retrofit:
  global-log:
    # 로그 출력 활성화 (기본 false)
    enable: false
    # 글로벌 로그 출력 레벨
    log-level: info
    # 글로벌 로그 출력 전략 (기본 BASIC, 요청/응답 행만 출력)
    log-strategy: basic
    # 요청 로그를 집합 출력할지
    aggregate: true
    # 로그 이름. 기본값은 LoggingInterceptor의 전체 클래스명
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 로그에서 숨겨야 하는 민감한 요청 헤더
    # 기본 마스크: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # 주의: 사용자가 이 설정을 구성하면 기본값 전체가 오버라이드됩니다. 마스크할 항목을 포함해야 합니다
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie
```

4가지 로그 출력 전략의 의미는 다음과 같습니다:

1. **NONE**: 로그를 출력하지 않음
2. **BASIC**: 요청과 응답 행만 출력
3. **HEADERS**: 요청과 응답 행 및 요청 헤더/응답 헤더 출력
4. **BODY**: 요청과 응답 행, 요청 헤더/응답 헤더, 요청 본체/응답 본체(있는 경우) 출력

## 선언형 로그 출력

일부 요청만 로그를 출력하려면, 관련 인터페이스 또는 메서드에서 `@Logging` 어노테이션을 사용할 수 있습니다.

## 커스텀 확장

로그 출력 동작을 수정하려면, `LoggingInterceptor`를 상속하고 Spring Bean으로 설정할 수 있습니다.

---

[이전 섹션: 메서드 수준 타임아웃 설정](timeout.md) | [다음 섹션: 요청 재시도](retry.md)