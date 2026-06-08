# 서킷브레이커/폴백
[English](../en/degrade.md) | [简体中文](../cn/degrade.md) | [繁體中文](../tw/degrade.md) | [日本語](../ja/degrade.md) | **한국어** | [Español](../es/degrade.md) | [Türkçe](../tr/degrade.md) | [Русский](../ru/degrade.md)

서킷브레이커/폴백은 기본값으로 비활성화되어 있으며, 현재 **Sentinel**과 **Resilience4j** 두 가지 구현을 지원합니다.

```yaml
retrofit:
  degrade:
    # 서킷브레이커/폴백 타입, 기본값 none은 비활성화를 의미
    degrade-type: sentinel
```

## Sentinel

### 의존성 추가

Sentinel 의존성을 수동으로 추가합니다:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

### 선언형 서킷브레이커

`degrade-type=sentinel`로 활성화한 후, 관련 인터페이스 또는 메서드에 `@SentinelDegrade` 어노테이션을 선언합니다:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = SentinelFallbackUserService.class)
@SentinelDegrade(rules = {
    @SentinelDegradeRule(grade = 0, count = 100, timeWindow = 4),
    @SentinelDegradeRule(grade = 1, count = 0.01, timeWindow = 3)
})
public interface SentinelUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @SentinelDegrade(rules = {@SentinelDegradeRule(grade = 2, count = 1, timeWindow = 6)})
    User getUser(@Query("id") Long id);
}
```

### 전역 Sentinel 서킷브레이커/폴백

```yaml
retrofit:
  global-sentinel-degrade:
    enable: true
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
```

## Resilience4j

### 의존성 추가

Resilience4j 의존성을 수동으로 추가합니다:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>1.7.1</version>
</dependency>
```

### 서킷브레이커 설정 등록

`CircuitBreakerConfigRegistrar` 인터페이스를 구현하여 `CircuitBreakerConfig`를 등록합니다:

```java
@Component
public class CustomCircuitBreakerConfigRegistrar implements CircuitBreakerConfigRegistrar {
    @Override
    public void register(CircuitBreakerConfigRegistry registry) {
        // 기본 CircuitBreakerConfig를 교체
        registry.register(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());

        // 다른 CircuitBreakerConfig를 등록
        registry.register("testCircuitBreakerConfig", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(20)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build());
    }
}
```

### 선언형 서킷브레이커

`degrade-type=resilience4j`로 활성화한 후, 관련 인터페이스 또는 메서드에 `@Resilience4jDegrade`를 선언합니다:

```java
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = Resilience4jFallbackFactory.class)
@Resilience4jDegrade(circuitBreakerConfigName = "testCircuitBreakerConfig")
public interface Resilience4jUserService {

    @POST("getName")
    String getName(@Query("id") Long id);

    @GET("getUser")
    @Resilience4jDegrade(enable = false)
    User getUser(@Query("id") Long id);
}
```

### 전역 Resilience4j 서킷브레이커/폴백

```yaml
retrofit:
  degrade:
    degrade-type: resilience4j
    global-resilience4j-degrade:
      enable: true
      # 이 이름으로 CircuitBreakerConfigRegistry에서 CircuitBreakerConfig를 가져와 전역 서킷브레이커 설정으로 사용
      circuit-breaker-config-name: defaultCircuitBreakerConfig
```

`circuitBreakerConfigName`으로 `CircuitBreakerConfig`를 지정할 수 있습니다, `retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name` 또는 `@Resilience4jDegrade.circuitBreakerConfigName`을 통해.

## 서킷브레이커/폴백 확장

다른 서킷브레이커/폴백 구현을 사용해야 하는 경우, `BaseRetrofitDegrade`를 상속하고 Spring Bean으로 구성할 수 있습니다.

## Fallback과 FallbackFactory

`@RetrofitClient`에 `fallback` 또는 `fallbackFactory`를 설정하지 않으면, 서킷브레이커가 트리거될 때 `RetrofitBlockException` 예외가 직접 발생합니다. 사용자는 `fallback` 또는 `fallbackFactory`를 설정하여 서킷브레이커 시 메서드 반환값을 커스텀할 수 있습니다.

> 참고: `fallback` 클래스는 현재 인터페이스의 구현 클래스必须, `fallbackFactory`는 `FallbackFactory<T>` 구현 클래스必须, 제네릭 파라미터 타입은 현재 인터페이스 타입입니다. 또한, `fallback`과 `fallbackFactory` 인스턴스는 Spring Bean으로 구성必须합니다.

`fallbackFactory`는 `fallback`과 비교하여, 주요 차이는每次 서킷브레이커의 예외 원인(cause)을感知할 수 있다는 점입니다.

### Fallback 예시

```java
@Slf4j
@Service
public class HttpDegradeFallback implements HttpDegradeApi {

    @Override
    public Result<Integer> test() {
        Result<Integer> fallback = new Result<>();
        fallback.setCode(100)
                .setMsg("fallback")
                .setBody(1000000);
        return fallback;
    }
}
```

### FallbackFactory 예시

```java
@Slf4j
@Service
public class HttpDegradeFallbackFactory implements FallbackFactory<HttpDegradeApi> {

    @Override
    public HttpDegradeApi create(Throwable cause) {
        log.error("서킷브레이커가 트리거되었습니다! ", cause.getMessage(), cause);
        return new HttpDegradeApi() {
            @Override
            public Result<Integer> test() {
                Result<Integer> fallback = new Result<>();
                fallback.setCode(100)
                        .setMsg("fallback")
                        .setBody(1000000);
                return fallback;
            }
        };
    }
}
```

---

[이전: 인터셉터](interceptor.md) | [다음: 오류 디코더](error-decoder.md)