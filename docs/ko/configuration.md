# 전체 설정 항목 레퍼런스
[English](../en/configuration.md) | [简体中文](../cn/configuration.md) | [繁體中文](../tw/configuration.md) | [日本語](../ja/configuration.md) | **한국어** | [Español](../es/configuration.md) | [Türkçe](../tr/configuration.md) | [Русский](../ru/configuration.md)

컴포넌트는 여러 설정 가능한 속성을 지원하여, 다양한 비즈니스 시나리오에 대응합니다. 다음은 모든 설정 속성과 기본값입니다:

```yaml
retrofit:
  # 글로벌 컨버터 팩토리 (기본 JacksonConverterFactory)
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory

  # 글로벌 적응 팩토리 (컴포넌트 확장 CallAdapterFactory는 내장됨, 중복 설정하지 마세요)
  global-call-adapter-factories:
    # ...

  # 글로벌 로그 출력 설정
  global-log:
    # 로그 출력 활성화 (기본 false)
    enable: false
    # 글로벌 로그 출력 레벨
    log-level: info
    # 글로벌 로그 출력 전략 (기본 BASIC)
    log-strategy: basic
    # 요청 로그 집합 출력 여부
    aggregate: true
    # 로그 이름. 기본값은 LoggingInterceptor의 전체 클래스명
    logName: com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor
    # 로그에서 숨겨야 하는 민감한 요청 헤더
    # 기본 마스크: Authorization, Proxy-Authorization, Cookie, Set-Cookie
    # 주의: 사용자가 이 설정을 구성하면 기본값 전체가 오버라이드됩니다
    redact-headers:
      - Authorization
      - Proxy-Authorization
      - Cookie
      - Set-Cookie

  # 글로벌 재시도 설정
  global-retry:
    # 글로벌 재시도 활성화 여부
    enable: false
    # 글로벌 재시도 기본 간격 시간 (밀리초)
    interval-ms: 100
    # 글로벌 최대 재시도 횟수
    max-retries: 2
    # 백오프 전략: FIXED (고정 간격, 기본값) / EXPONENTIAL (지수 백오프)
    backoff-strategy: fixed
    # 지수 백오프 간격 상한 (밀리초), EXPONENTIAL만 유효
    max-interval-ms: 30000
    # 지터 계수 [0.0, 1.0], 0.0은 지터 없음
    jitter: 0.0
    # 글로벌 재시도 규칙
    retry-rules:
      - response_status_not_2xx
      - occur_io_exception

  # 글로벌 타임아웃 설정
  global-timeout:
    # 글로벌 읽기 타임아웃 시간 (밀리초)
    read-timeout-ms: 10000
    # 글로벌 쓰기 타임아웃 시간 (밀리초)
    write-timeout-ms: 10000
    # 글로벌 연결 타임아웃 시간 (밀리초)
    connect-timeout-ms: 10000
    # 글로벌 전체 호출 타임아웃 시간 (밀리초), 0은 타임아웃 없음
    call-timeout-ms: 0

  # 글로벌 연결 풀 설정
  global-connection-pool:
    # 최대 idle 연결 수
    max-idle-connections: 5
    # 연결 유지 시간 (밀리초)
    keep-alive-duration-ms: 300000

  # 메트릭 모니터링 설정 (기본 비활성; enable=true 명시 설정 필요. 컨테이너 내 MeterRegistry 필수)
  metrics:
    # 활성화 여부, 기본 false
    enable: false
    # Timer 퍼센타일
    percentiles: [0.5, 0.95, 0.99]
    # SLO 히스토그램 버킷
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # host 태그 포함 여부
      host: false
      # uri 태그 포함 여부
      uri: true
    # 글로벌 추가 태그
    extra-tags:
      app: my-service
    # 메트릭명 프리픽스
    metric-name-prefix: retrofit.client

  # 서킷브레이커/데그레이드 설정
  degrade:
    # 서킷브레이커/데그레이드 타입. 기본 none, 비활성
    degrade-type: none
    # 글로벌 Sentinel 데그레이드 설정
    global-sentinel-degrade:
      # 활성화 여부
      enable: false
      rules:
        # 데그레이드 전략 (0: 평균 응답 시간; 1: 예외 비율; 2: 예외 수)
        - grade: 0
          # 각 데그레이드 전략에 해당하는 임계값
          count: 1000
          # 서킷브레이커 시간 (초)
          time-window: 5
          # 서킷브레이커 트리거 최소 요청 수
          min-request-amount: 5
          # RT 모드 느린 요청 비율 임계값
          slow-ratio-threshold: 1.0
          # 통계 간격 시간 (밀리초)
          stat-interval-ms: 1000
    # 글로벌 Resilience4j 데그레이드 설정
    global-resilience4j-degrade:
      # 활성화 여부
      enable: false
      # CircuitBreakerConfig를 가져오는 이름. 글로벌 서킷브레이커 설정으로 사용
      circuit-breaker-config-name: defaultCircuitBreakerConfig

  # PathMatchInterceptor의 scope을 prototype으로 자동 설정
  auto-set-prototype-scope-for-path-math-interceptor: true
  # ErrorDecoder 기능 활성화 여부
  enable-error-decoder: true
```

대부분의 시나리오에서 Spring Boot 설정 파일(application.yml 또는 application.properties)에 위 설정을 추가하면 컴포넌트 기능을 커스터마이징할 수 있습니다. 설정이 유효하지 않는 문제가 있는 경우 [자주 묻는 질문](faq.md)을 참조하세요.

**Spring Boot 설정 파일이 유효하지 않는 경우, RetrofitProperties Bean을 수동으로 설정할 수 있습니다**. 코드는 다음과 같습니다:

```java
@Bean
public RetrofitProperties retrofitProperties() {
    RetrofitProperties retrofitProperties = new RetrofitProperties();
    // retrofitProperties의 각 설정 값을 수동으로 수정
    return retrofitProperties;
}
```

---

[이전 섹션: RetrofitClient 어노테이션 커스터마이징](custom-annotation.md) | [다음 섹션: 기타 기능 예제](examples.md)