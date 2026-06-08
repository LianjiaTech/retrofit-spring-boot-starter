# 메트릭 모니터링 (Micrometer)
[English](../en/metrics.md) | [简体中文](../cn/metrics.md) | [繁體中文](../tw/metrics.md) | [日本語](../ja/metrics.md) | **한국어** | [Español](../es/metrics.md) | [Türkçe](../tr/metrics.md) | [Русский](../ru/metrics.md)

컴포넌트는 [Micrometer](https://micrometer.io/) 기반의 메트릭 수집 기능을 내장하고 있습니다. **기본적으로 비활성화**이며, `retrofit.metrics.enable=true`를 명시적으로 설정해야 활성화됩니다.

> **기본 비활성화, 명시적 활성화의 이유**: Spring Boot autoconfig 간에 안정적인 로드 순서 제약이 없으며, `@ConditionalOnBean(MeterRegistry.class)`에 의존하는 자동 활성화는 평가 시점 문제로 "actuator를 도입했는데 메트릭이 없는" 잠재적 실패를 유발합니다. opt-in으로 변경 후 동작은 완전히 예측 가능합니다: actuator를 도입해도 자동으로 instrumentation되지 않습니다. 명시적 활성화 시 컨테이너 내에 `MeterRegistry`가 없으면, 시작 시 빠르게 실패하여 조용한 메트릭 누락이 되지 않습니다.

## 활성화 방법

1. Micrometer와 해당 모니터링 백엔드(Prometheus / Datadog / Atlas 등)를 도입합니다. Spring Boot Actuator는 `MeterRegistry`를 등록합니다:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. 설정에서 명시적으로 활성화합니다:

```yaml
retrofit:
  metrics:
    enable: true
```

## 수집되는 메트릭

| 메트릭명 | 타입 | 의미 |
|---|---|---|
| `retrofit.client.requests` | Timer | 각 HTTP 호출 소요 시간 분포 (퍼센타일과 SLO 히스토그램 포함) |
| `retrofit.client.requests.active` | LongTaskTimer | 진행 중인 요청 수와 최대 생존 시간 |
| `retrofit.client.errors` | Counter | 요청 예외 카운트 (exception 클래스명 디멘션별) |

## 태그 디멘션

기본 tag (基数有界, Prometheus 등 고基数 민감 백엔드에서 안전하게 사용 가능):

| Tag | 의미 | 값 예시 |
|---|---|---|
| `client` | Retrofit 인터페이스의 단순 클래스명 | `UserService` |
| `method` | Java 메서드명 | `getUser` |
| `http.method` | HTTP 메서드 | `GET`/`POST` |
| `uri` | 어노테이션의 경로 템플릿 (`@Path`를 확장하지 않음) | `user/{id}` |
| `status` | 상태 코드 버킷 | `2xx`/`3xx`/`4xx`/`5xx`/`IO_ERROR` |
| `outcome` | 비즈니스 결과 | `SUCCESS`/`CLIENT_ERROR`/`SERVER_ERROR`/`IO_ERROR` |
| `exception` | errors 메트릭만, 예외 클래스명 | `SocketTimeoutException` |

> **주의**: tag 값은 有界集合이어야 합니다. 따라서 `uri` 태그는 어노테이션의 경로 템플릿(`{id}` 플레이스홀더 포함)을 사용하며, 확장된 실제 URL은 사용하지 않습니다. 이렇게 하면 동적 경로 매개변수로 인한 메트릭基数 폭발을 방지할 수 있습니다.

## 설정 항목

```yaml
retrofit:
  metrics:
    # 활성화 여부, 기본 false. true로 설정해야 metrics 인터셉터가 구축됩니다
    enable: true
    # Timer 발행 퍼센타일; 빈 배열은 발행하지 않음
    percentiles: [0.5, 0.95, 0.99]
    # SLO 히스토그램 버킷; 빈 배열은 히스토그램을 발행하지 않음
    sla:
      - 50ms
      - 100ms
      - 300ms
      - 1s
      - 3s
    tags:
      # host 태그 포함 여부, 기본 비활성 (동적 baseUrl 시나리오에서 host 수가 많을 수 있음)
      host: false
      # uri 태그 포함 여부, 기본 활성
      uri: true
    # 글로벌 정적 추가 태그
    extra-tags:
      app: my-service
      env: prod
    # 메트릭명 프리픽스, 기본 retrofit.client
    metric-name-prefix: retrofit.client
```

## 커스텀 태그

기본 tag 디멘션이 요구를 충족하지 않는 경우, `RetrofitTagsProvider` 인터페이스를 구현하여 Spring Bean으로 등록하면 기본 구현이 자동으로 오버라이드됩니다:

```java
@Component
public class TenantAwareTagsProvider implements RetrofitTagsProvider {

    private final RetrofitTagsProvider delegate;

    public TenantAwareTagsProvider(MetricsProperty property) {
        this.delegate = new DefaultRetrofitTagsProvider(property);
    }

    @Override
    public Tags getTags(Request request, Response response, Throwable exception) {
        return delegate.getTags(request, response, exception)
                .and("tenant", TenantContext.current());
    }
}
```

> 커스텀 구현 시 tag 값集合이有界인 것, tag 순서와 이름이安定한 것을 반드시 보장하세요. 그렇지 않으면 Micrometer가 여러 의미 없는 Meter를 생성하여 메모리 낭비를 유발합니다.

---

[이전 섹션: 에러 디코더](error-decoder.md) | [다음 섹션: Actuator Endpoint](actuator.md)