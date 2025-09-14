# REST Client Spring6 - 프로젝트 컨텍스트

## 프로젝트 개요

**rest-client-spring6**는 Spring WebFlux의 WebClient를 기반으로 한 HTTP 요청/응답 라이브러리입니다. 비동기 reactive 프로그래밍을 지원하며, 선언형 HTTP 요청 구조와 일관된 응답 형식을 제공합니다.

### 기본 정보
- **Group ID**: `io.incognito`
- **Artifact ID**: `rest-client-spring6`
- **Version**: `2.0.1-RELEASE`
- **Java Version**: 17
- **배포**: JitPack을 통한 배포 지원

## 핵심 기술 스택

### 주요 Dependencies
- **Spring WebFlux** 6.2.10 - reactive 웹 클라이언트
- **Project Reactor** 3.7.11 - reactive 프로그래밍 모델
- **Reactor Netty** 1.2.10 - 비동기 네트워킹
- **Jackson** 2.19.2 - JSON 직렬화/역직렬화
- **Netty** 4.2.6.Final - 고성능 네트워크 프레임워크
- **Lombok** 1.18.30 - 보일러플레이트 코드 제거

## 아키텍처 구조

### 1. 핵심 컴포넌트

#### HTTP 클라이언트 실행기
- **`HttpClientExecutors<AUTH>`**: 추상 기본 클래스로 HTTP 요청 실행 로직 제공
- **`HttpClients<AUTH>`**: Builder 패턴을 사용한 HTTP 클라이언트 팩토리

#### 요청/응답 인터페이스
- **`IHttpRequest<AUTH>`**: HTTP 요청 스펙 정의 인터페이스
- **`IBaseResponse`**: 모든 응답 객체의 기본 인터페이스

#### 응답 구조
- **`ApiResult`**: HTTP 응답의 메타데이터와 결과 정보
- **`BaseApiResponse`**: 모든 API 응답의 기본 클래스
- **`EmptyOrStringBodyResponse`**: 빈 응답 또는 문자열 응답 처리

### 2. 지원하는 HTTP 메서드

```java
// GET 요청
HttpClients.get(webClient)
    .url("/api/endpoint")
    .build()
    .executeAsync(ResponseType.class);

// POST 요청 (JSON Body)
HttpClients.post(webClient)
    .url("/api/endpoint")
    .build()
    .executeWithBodyAsync(requestObj, ResponseType.class);

// POST 요청 (Form Data)
HttpClients.post(webClient)
    .url("/api/endpoint")
    .build()
    .executeWithFormDataAsync(formDataBuilder, ResponseType.class);
```

### 3. 인증 시스템

```java
public interface AuthorizationApplier<AUTH> {
    <S extends WebClient.RequestHeadersSpec<?>> void apply(S httpRequest, AUTH auth);
}
```

- Generic 타입 `<AUTH>`를 통한 유연한 인증 방식 지원
- 함수형 인터페이스로 다양한 인증 방식 구현 가능

### 4. 반응형(Reactive) 프로그래밍

#### Project Reactor 기반 비동기 처리
- 모든 HTTP 요청은 `Mono<T>` 반환
- 백프레셔(Backpressure) 지원
- 논블로킹 I/O 연산

#### 에러 핸들링 및 재시도
```java
// 재시도 설정 예시
HttpClients.post(webClient)
    .url("/api/endpoint")
    .retryCount(3)
    .retryDelay(Duration.ofSeconds(1))
    .build()
    .executeAsync(ResponseType.class);
```

#### 지원하는 예외 처리
- `ReadTimeoutException` - 응답 시간 초과
- `SslHandshakeTimeoutException` - SSL 핸드셰이크 타임아웃
- `WebClientRequestException` - 네트워크 연결 실패
- `ApiFailureException` - API 호출 실패

### 5. 응답 처리 시스템

#### ApiResultCode 열거형
```java
public enum ApiResultCode {
    SUCCESS("0000", "성공", "Success"),
    INVALID_PARAMETER("1000", "파라미터 오류", "Invalid parameter"),
    INVALID_RESPONSE("1002", "응답 오류", "Invalid response"),
    INVALID_AUTH("1003", "인증 오류", "Invalid authentication"),
    INVALID_SYSTEM("1004", "시스템 오류", "System Error"),
    // ... 기타 오류 코드들
}
```

#### HTTP 상태 코드별 처리
- **2xx**: 성공 처리
- **3xx**: 자동 리다이렉트 처리 (Location 헤더 기반)
- **4xx/5xx**: 오류로 처리하여 ApiFailureException 발생

### 6. 네트워크 구성

#### HttpClientConfigurer 추상 클래스
```java
public abstract class HttpClientConfigurer {
    // Connection Pool 설정
    public ConnectionProvider httpConnectionPool();

    // Netty HTTP Client 설정
    public HttpClient httpApiClient();

    // WebClient 설정
    public WebClient apiWebClient(List<MimeType> serializeMimeTypes,
                                  List<MimeType> deserializeMimeTypes);
}
```

#### 네트워크 설정 특징
- Connection Pool 지원 (최대 연결 수 제한)
- TCP Keep-Alive 설정
- SSL/TLS 지원 (InsecureTrustManagerFactory 사용)
- 타임아웃 설정 (연결, 읽기, 쓰기)

### 7. 콜백 시스템

#### HttpCallbackHandler 인터페이스
```java
public interface HttpCallbackHandler<RESP> {
    void onResponse(RESP response, IHttpApiContext<?> context);
    void onError(Throwable error, IHttpApiContext<?> context);
    void afterFinished(SignalType signalType, IHttpApiContext<?> context);
}
```

## 사용 패턴 및 예시

### 1. 기본 사용 패턴

```java
// WebClient 생성
WebClient webClient = WebClient.create("https://api.example.com");

// GET 요청
Mono<UserResponse> userMono = HttpClients.get(webClient)
    .url("/users/{id}")
    .pathVariable("id", "123")
    .queryParam("include", "profile")
    .build()
    .executeAsync(UserResponse.class);

// POST 요청
Mono<CreateResponse> createMono = HttpClients.post(webClient)
    .url("/users")
    .build()
    .executeWithBodyAsync(createUserRequest, CreateResponse.class);
```

### 2. 폼 데이터 전송

```java
MultipartBodyBuilder formBuilder = new MultipartBodyBuilder();
formBuilder.part("file", fileResource);
formBuilder.part("metadata", metadataObj);

Mono<UploadResponse> uploadMono = HttpClients.post(webClient)
    .url("/upload")
    .build()
    .executeWithFormDataAsync(formBuilder, UploadResponse.class);
```

### 3. 콜백을 사용한 라이프사이클 관리

```java
HttpCallbackHandler<UserResponse> handler = new HttpCallbackHandler<UserResponse>() {
    @Override
    public void onResponse(UserResponse response, IHttpApiContext<?> context) {
        log.info("요청 성공: {}", response);
    }

    @Override
    public void onError(Throwable error, IHttpApiContext<?> context) {
        log.error("요청 실패: {}", error.getMessage());
    }

    @Override
    public void afterFinished(SignalType signalType, IHttpApiContext<?> context) {
        log.info("요청 완료: {}", signalType);
    }
};

HttpClients.get(webClient)
    .url("/users/123")
    .build()
    .executeAsync(UserResponse.class, handler);
```

## 주요 특징 및 장점

### 1. 선언형 API
- Builder 패턴을 통한 직관적인 요청 구성
- 메서드 체이닝으로 가독성 향상

### 2. Type-Safe 제네릭 지원
- 컴파일 타임 타입 검증
- 인증 방식의 유연한 제네릭 지원

### 3. 완전한 비동기 처리
- Non-blocking I/O
- Reactive Streams 표준 준수
- 백프레셔 지원

### 4. 포괄적인 에러 핸들링
- 네트워크 수준 예외 처리
- HTTP 상태 코드별 세분화된 처리
- 재시도 메커니즘 내장

### 5. 자동 리다이렉트 처리
- 3xx 응답에 대한 자동 리다이렉트
- 절대/상대 경로 모두 지원

### 6. 확장성
- 추상 클래스 기반으로 커스터마이징 가능
- 플러그인 방식의 인증 시스템
- 콜백을 통한 라이프사이클 후킹

## 프로젝트 구조

```
src/main/java/io/incognito/rest/client/
├── AuthorizationApplier.java           # 인증 적용 함수형 인터페이스
├── HttpClientExecutors.java            # HTTP 실행기 추상 클래스
├── HttpClients.java                     # HTTP 클라이언트 빌더
├── IHttpRequest.java                    # HTTP 요청 인터페이스
├── config/
│   └── HttpClientConfigurer.java       # HTTP 클라이언트 설정
├── exceptions/
│   ├── ApiFailureException.java        # API 실패 예외
│   └── ApiFailureExceptionHandler.java # 예외 핸들러
├── handler/
│   └── HttpCallbackHandler.java        # HTTP 콜백 핸들러
├── helper/
│   ├── ClientResponseProcessor.java    # 응답 처리기
│   └── ResponseTypeConverter.java      # 응답 타입 변환기
├── types/
│   ├── IHttpApiContext.java            # HTTP API 컨텍스트 인터페이스
│   ├── dto/
│   │   ├── ApiResult.java              # API 결과 객체
│   │   ├── ClientContextImpl.java      # 클라이언트 컨텍스트 구현
│   │   ├── request/
│   │   │   └── MultipartFormDataRequest.java
│   │   └── response/
│   │       ├── BaseApiResponse.java    # 기본 API 응답
│   │       ├── EmptyOrStringBodyResponse.java
│   │       ├── IBaseResponse.java      # 기본 응답 인터페이스
│   │       ├── ListResponse.java       # 리스트 응답
│   │       ├── MapResponse.java        # 맵 응답
│   │       └── StringObjectMapResponse.java
│   └── enums/
│       └── ApiResultCode.java          # API 결과 코드 열거형
└── util/                               # 유틸리티 클래스들
    ├── Maps.java
    ├── MultiValueMaps.java
    ├── Opt.java
    └── TypeUtil.java
```

## 개발 시 고려사항

### 1. 메모리 관리
- Reactive Streams의 비동기 특성상 메모리 리크 방지를 위한 적절한 구독 해제 필요
- Connection Pool을 통한 리소스 효율적 사용

### 2. 에러 처리
- `ApiFailureException`을 통한 일관된 에러 응답 제공
- 재시도 횟수와 지연 시간 적절히 설정

### 3. 테스트
- WebTestClient를 활용한 Integration Test 권장
- StepVerifier를 사용한 Reactive 코드 테스트

### 4. 성능 최적화
- Connection Pool 크기 조정
- Netty 이벤트 루프 스레드 수 최적화
- JSON 직렬화/역직렬화 성능 고려

이 라이브러리는 Spring WebFlux 환경에서 HTTP 클라이언트를 효율적이고 안전하게 사용할 수 있도록 설계된 포괄적인 솔루션입니다.