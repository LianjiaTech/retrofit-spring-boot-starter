# 메서드급 타임아웃 설정
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | **한국어** | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

컴포넌트는 `@Timeout` 어노테이션을 통해 메서드 또는 클래스 수준에서 타임아웃 파라미터를 설정하여, 전역 타임아웃 설정을 오버라이드할 수 있습니다.

## 우선순위 체인

```
메서드 @Timeout → 클래스 @Timeout → 전역 설정(GlobalTimeoutProperty)
```

- `@Timeout` 속성 기본값 `-1`은 "구성되지 않음, 상위 우선순위 체인에서 상속"을 의미
- `0`으로 설정하면 "타임아웃 없음"을 의미
- 양수로 설정하면 구체적인 타임아웃 밀리초 값을 의미
- `-1`은 OkHttp 타임아웃의 불법 값 영역(OkHttp는 0과 양수만 허용)이므로, "구성되지 않음" 마커로 사용해도 합법적인 타임아웃 값과 충돌하지 않습니다

## 4가지 타임아웃 차원

| 속성 | 의미 | 기본값 |
|------|------|--------|
| `connectTimeoutMs` | 연결 타임아웃(밀리초) | `-1`(상위에서 상속) |
| `readTimeoutMs` | 읽기 타임아웃(밀리초) | `-1`(상위에서 상속) |
| `writeTimeoutMs` | 쓰기 타임아웃(밀리초) | `-1`(상위에서 상속) |
| `callTimeoutMs` | 전체 호출 타임아웃(밀리초) | `-1`(상위에서 상속) |

## 클래스급 @Timeout

인터페이스에 `@Timeout`을 선언하여, 해당 인터페이스의 모든 메서드에 타임아웃을 설정합니다:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 메서드급 @Timeout

특정 메서드에 `@Timeout`을 선언하여, 클래스급 또는 전역 타임아웃 설정을 오버라이드합니다:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 클래스급 readTimeoutMs = 5000를 상속
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 메서드급 오버라이드: 느린 조회 인터페이스는 더 긴 타임아웃 사용
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 메서드급 @Timeout만 사용(클래스급 어노테이션 없음)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 전역 타임아웃 설정 사용
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 이 메서드만 커스텀 타임아웃 사용
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

---

[이전: 커스텀 OkHttpClient](okhttp-client.md) | [다음: 로그 출력](logging.md)