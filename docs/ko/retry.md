# 요청 재시도
[English](../en/retry.md) | [简体中文](../cn/retry.md) | [繁體中文](../tw/retry.md) | [日本語](../ja/retry.md) | **한국어** | [Español](../es/retry.md) | [Türkçe](../tr/retry.md) | [Русский](../ru/retry.md)

컴포넌트는 글로벌 재시도와 선언형 재시도를 지원합니다.

## 글로벌 재시도

글로벌 재시도는 기본적으로 비활성화되어 있습니다. 기본 설정 항목은 다음과 같습니다:

```yaml
retrofit:
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
```

### 재시도 규칙

재시도 규칙은 3가지 설정을 지원합니다:

1. **RESPONSE_STATUS_NOT_2XX**: 응답 상태 코드가 2xx가 아닌 경우 재시도 실행
2. **OCCUR_IO_EXCEPTION**: IO 예외 발생 시 재시도 실행
3. **OCCUR_EXCEPTION**: 임의의 예외 발생 시 재시도 실행

### 백오프 전략과 지터

`backoffStrategy`는 재시도 간격의 증가 방식을 제어합니다. 기본값 `FIXED`는 이전 동작과 일치합니다:

- **FIXED**: 각 재시도 간격이 `intervalMs`로 고정
- **EXPONENTIAL**: 지수 백오프. N번째 재시도 간격 = `intervalMs × 2^N` (N은 0부터 시작). `maxIntervalMs`로 상한을 설정하여 간격 무한 증가를 방지

`jitter` (값 범위 `[0.0, 1.0]`, 기본값 `0.0`은 지터 없음)은 계산 지연에 랜덤 지터를 추가하여, 여러 클라이언트의 동시 재시도로 인한 thundering herd effect를 방지합니다:

> 실제 지연 = 계산 지연 × (1 + jitter × random), random은 `[0, 1)`의 랜덤 수

### 조건 트리거: 상태 코드 / 예외 타입별

`RetryRule`의 조粗粒度 규칙基础上에서 트리거 조건을 더 세밀하게 제한할 수 있습니다 (기본값은 빈 값, 이전 동작과 일치):

- `retryStatusCodes`: 응답 상태 코드가 목록에 일치하는 경우만 재시도 (`RESPONSE_STATUS_NOT_2XX` 규칙과 함께 필요). 예: `{502, 503, 504}`
- `retryExceptionClasses`: 예외 타입이 목록에 일치하는 경우만 재시도 (`RetryRule`에 일치하는 예외基础上에서 더 세밀하게 제한). 예: `{SocketTimeoutException.class}`

```java
@RetrofitClient(baseUrl = "http://localhost:8080/")
@Retry(maxRetries = 3, intervalMs = 200, backoffStrategy = BackoffStrategy.EXPONENTIAL,
        maxIntervalMs = 5000, jitter = 0.3, retryStatusCodes = {502, 503, 504})
public interface Api {
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 선언형 재시도

일부 요청만 재시도가 필요한 경우, 해당 인터페이스 또는 메서드에서 `@Retry` 어노테이션을 사용할 수 있습니다.

## 커스텀 확장

요청 재시도 동작을 수정하려면, `RetryInterceptor`를 상속하고 Spring Bean으로 설정할 수 있습니다.

---

[이전 섹션: 로그 출력](logging.md) | [다음 섹션: 인터셉터](interceptor.md)