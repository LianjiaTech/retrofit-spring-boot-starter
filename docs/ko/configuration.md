# 전체 설정 항목 참조
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | **한국어** | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

컴포넌트는 여러 설정 속성을 지원하여, 다양한 비즈니스 시나리오에対応합니다. 모든 설정 속성과 기본값은 다음과 같습니다:

```yaml
retrofit:
  # 전역 변환기 팩토리(기본값 JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # 전역 어댑터 팩토리(컴포넌트가 확장한 CallAdapterFactory는 이미 내장되어 있으므로 중복 설정하지 마세요)
  global-call-adapter-factories:
    # ...

  # 전역 로그 출력 설정
  global-log:
    # 로그 출력 활성화(기본값 false)
    enable: false
    # 전역 로그 출력 레벨
    log-level: info
    # 전역 로그 출력 전략(기본값 BASIC)
    log-strategy: basic
    # 요청 로그를 집계 출력할지 여부
    aggregate: true
    # 로그 이름, 기본값은 LoggingInterceptor의 전체 클래스명
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 로그에서 숨겨야 할 민감 요청 헤더
    # 기본값 마스킹: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # 참고: 사용자가 이 항목을 설정하면 기본값을 전체적으로 오버라이드하므로, 마스킹할 항목을自行으로 포함해야 합니다
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # 전역 재시도 설정
  global-retry:
    # 전역 재시도 활성화 여부
    enable: false
    # 전역 재시도 기본 간격 시간(밀리초)
    interval-ms: 100
    # 전역 최대 재시도 횟수
    max-retries: 2
    # 백오프 전략: FIXED(고정 간격, 기본값) / EXPONENTIAL(지수 백오프)
    backoff-strategy: fixed
    # 지수 백오프 간격 상한(밀리초), EXPONENTIAL만 적용
    max-interval-ms: 30000
    # 지터 계수 [0.0, 1.0], 0.0은 지터 없음
    jitter: 0.0
    # 전역 재시도 규칙
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # 전역 타임아웃 시간 설정
  global-timeout:
    # 전역 읽기 타임아웃 시간(밀리초)
    read-timeout-ms: 10000
    # 전역 쓰기 타임아웃 시간(밀리초)
    write-timeout-ms: 10000
    # 전역 연결 타임아웃 시간(밀리초)
    connect-timeout-ms: 10000
    # 전역 전체 호출 타임아웃 시간(밀리초), 0은 타임아웃 없음을 의미
    call-timeout-ms: 0

  # 전역 연결 풀 설정
  global-connection-pool:
    # 최대 대기 연결 수
    max-idle-connections: 5
    # 연결 유지 시간(밀리초)
    keep-alive-duration-ms: 300000

  # 서킷브레이커/폴백 설정
  degrade:
    # 서킷브레이커/폴백 타입. 기본값 none, 서킷브레이커/폴백 비활성화를 의미
    degrade-type: none
    # 전역 Sentinel 폴백 설정
    global-sentinel-degrade:
      # 활성화 여부
      enable: false
      rules:
        # 폴백 전략(0: 평균 응답 시간; 1: 예외 비율; 2: 예외 수)
        - grade: 0
          # 각 폴백 전략에 해당하는 임계값. 평균 응답 시간(ms), 예외 비율(0-1), 예외 수(1-N)
          count: 1000
          # 서킷브레이커 시간, 단위는 s
          time-window: 5
          # (유효 통계 시간 범위 내) 서킷브레이커를 트리거할 최소 요청 수
          min-request-amount: 5
          # RT 모드下慢请求率의 임계값
          slow-ratio-threshold: 1.0
          # 시간 간격 통계 지속 시간, 단위는 밀리초
          stat-interval-ms: 1000
    # 전역 Resilience4j 폴백 설정
    global-resilience4j-degrade:
      # 활성화 여부
      enable: false
      # 이 이름으로 CircuitBreakerConfigRegistry에서 CircuitBreakerConfig를 가져와 전역 서킷브레이커 설정으로 사용
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # PathMatchInterceptor의 scope를 prototype으로 자동 설정
  auto-set-prototype-scope-for-path-math-interceptor: true
  # ErrorDecoder 기능 활성화 여부
  enable-error-decoder: true
```

대부분의 시나리오에서, Spring Boot 설정 파일(application.yml 또는 application.properties)에 위 설정을 추가하면 컴포넌트 기능을 커스텀 수정할 수 있습니다. 설정이 적용되지 않는 등的问题은 [자주 묻는 질문](faq.md)을 참조하세요.

---

[이전: 커스텀 RetrofitClient 어노테이션](custom-annotation.md) | [다음: 다른 기능 예시](examples.md)