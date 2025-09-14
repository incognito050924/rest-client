# Kotlin 사용 가이드 및 예제

## 구현 완료 사항

### ✅ 라이브러리 측 개선사항 (Phase 1 + Phase 3)

1. **Jackson Kotlin 모듈 추가** (pom.xml)
2. **ClientResponseProcessor Kotlin 호환성 개선**
3. **ResponseTypeConverter Kotlin 호환성 개선**
4. **KotlinCompatibilityUtil 유틸리티 클래스 생성**
5. **HttpClientConfigurer에 Kotlin 호환 Jackson 설정 추가**
6. **DefaultKotlinCompatibleHttpClientConfigurer 기본 구현체 제공**
7. **KotlinHttpClients 편의 클래스 제공**

## Kotlin에서 사용하기

### 1. 의존성 추가 (build.gradle.kts)

```kotlin
dependencies {
    implementation("io.incognito:rest-client-spring6-kotlin:2.0.1-RELEASE")

    // Jackson Kotlin 모듈 (라이브러리에 포함됨)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.4")

    // Coroutines 지원 (선택사항)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
}
```

### 2. 응답 DTO 클래스 작성

#### ✅ 권장 패턴

```kotlin
// 패턴 1: var 사용 (가장 안전하고 권장하는 방법)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserResponse(
    @JsonProperty("id") var id: Long = 0,        // var 사용!
    @JsonProperty("name") var name: String = "", // Jackson이 값 재할당 가능
    @JsonProperty("email") var email: String = ""
) : BaseApiResponse() {
    // Jackson을 위한 기본 생성자 (필수!)
    constructor() : this(0, "", "")
}

// 패턴 2: @JsonCreator로 val 유지 (불변성 선호 시)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserResponse @JsonCreator constructor(
    @JsonProperty("id") val id: Long = 0,      // val 사용 가능
    @JsonProperty("name") val name: String = "",
    @JsonProperty("email") val email: String = ""
) : BaseApiResponse() {
    constructor() : this(0, "", "")
}

// 패턴 3: Nullable var 타입 사용
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserResponse(
    @JsonProperty("id") var id: Long?,
    @JsonProperty("name") var name: String?,
    @JsonProperty("email") var email: String?
) : BaseApiResponse() {
    constructor() : this(null, null, null)
}
```

#### ❌ 피해야 할 패턴

```kotlin
// ❌ val + 기본값 + 기본 생성자 = Jackson 역직렬화 실패!
data class UserResponse(
    val id: Long = 0,      // val은 불변이라 Jackson이 값 재할당 불가!
    val name: String = ""
) : BaseApiResponse() {
    constructor() : this(0, "") // 기본값 초기화 후 Jackson이 변경 시도 → 실패
}

// ❌ 기본 생성자 없음 - 런타임 오류 발생!
data class UserResponse(
    var id: Long,
    var name: String
) : BaseApiResponse()
```

### 3. 간단한 사용법 (KotlinHttpClients 사용)

```kotlin
// 기본 WebClient 자동 생성
val userResponse: Mono<UserResponse> = KotlinHttpClients.get<String>()
    .url("/users/{id}")
    .pathVariable("id", "123")
    .build()
    .executeAsync(UserResponse::class.java)

// POST 요청
val createResponse: Mono<UserResponse> = KotlinHttpClients.post<String>()
    .url("/users")
    .build()
    .executeWithBodyAsync(createUserRequest, UserResponse::class.java)
```

### 4. 기존 HttpClients 사용법

```kotlin
class ApiService {
    // Kotlin 호환 WebClient 생성
    private val configurer = object : HttpClientConfigurer(30, 30, 30, 1024 * 1024, 100) {
        override fun connectionObserver() = ConnectionObserver.emptyListener()
        override fun webClientObjectMapper() = createKotlinCompatibleMapper()
    }

    private val webClient = configurer.apiWebClient(null, null)

    // GET 요청
    fun getUser(id: String): Mono<UserResponse> {
        return HttpClients.get<String>(webClient)
            .url("/users/{id}")
            .pathVariable("id", id)
            .queryParam("include", "profile")
            .build()
            .executeAsync(UserResponse::class.java)
    }

    // POST 요청
    fun createUser(request: CreateUserRequest): Mono<UserResponse> {
        return HttpClients.post<String>(webClient)
            .url("/users")
            .build()
            .executeWithBodyAsync(request, UserResponse::class.java)
    }

    // Coroutine과 함께 사용
    suspend fun getUserSuspend(id: String): UserResponse {
        return HttpClients.get<String>(webClient)
            .url("/users/{id}")
            .pathVariable("id", id)
            .build()
            .executeAsync(UserResponse::class.java)
            .awaitSingle()
    }
}
```

### 5. 폼 데이터 전송

```kotlin
fun uploadFile(file: Resource, metadata: FileMetadata): Mono<UploadResponse> {
    val formBuilder = MultipartBodyBuilder().apply {
        part("file", file)
        part("metadata", metadata)
    }

    return KotlinHttpClients.post<String>()
        .url("/upload")
        .build()
        .executeWithFormDataAsync(formBuilder, UploadResponse::class.java)
}
```

### 6. 콜백 핸들러 사용

```kotlin
val handler = object : HttpCallbackHandler<UserResponse> {
    override fun onResponse(response: UserResponse, context: IHttpApiContext<*>) {
        println("요청 성공: $response")
    }

    override fun onError(error: Throwable, context: IHttpApiContext<*>) {
        println("요청 실패: ${error.message}")
    }

    override fun afterFinished(signalType: SignalType, context: IHttpApiContext<*>) {
        println("요청 완료: $signalType")
    }
}

KotlinHttpClients.get<String>()
    .url("/users/123")
    .build()
    .executeAsync(UserResponse::class.java, handler)
```

## 주요 개선사항

### 🔧 자동 인스턴스 생성
- Kotlin data class의 기본 생성자 문제 해결
- 여러 생성자 시도를 통한 강건한 객체 생성
- Jackson을 활용한 fallback 메커니즘

### 🔧 Jackson Kotlin 모듈 통합
- 자동 모듈 감지 및 등록
- Kotlin 특화 직렬화/역직렬화 설정
- null 안전성 향상

### 🔧 편의 클래스 제공
- `KotlinHttpClients`: 기본 Kotlin 호환 WebClient 자동 생성
- `DefaultKotlinCompatibleHttpClientConfigurer`: 즉시 사용 가능한 설정

## 주의사항

### ⚠️ 여전히 필요한 것: DTO 기본 생성자
라이브러리가 Kotlin 호환성을 크게 개선했지만, 가장 안전한 방법은 여전히 **DTO 클래스에 기본 생성자를 명시적으로 추가**하는 것입니다.

```kotlin
data class MyResponse(
    var id: Long = 0,
    var name: String = ""
) : BaseApiResponse() {
    constructor() : this(0, "") // 여전히 권장!
}
```

### ⚠️ 성능 고려사항
- Kotlin 호환성 로직은 기본 생성자 실패 시에만 동작
- 기본 생성자가 있으면 기존과 동일한 성능

## 마이그레이션 가이드

### 기존 Java 코드에서
```java
// 기존 방식 그대로 사용 가능
HttpClients.get(webClient)
    .url("/users/123")
    .build()
    .executeAsync(UserResponse.class);
```

### 새로운 Kotlin 코드에서
```kotlin
// 편의 클래스 사용
KotlinHttpClients.get<String>()
    .url("/users/123")
    .build()
    .executeAsync(UserResponse::class.java)

// 또는 기존 방식에 Kotlin 호환 WebClient
HttpClients.get<String>(kotlinCompatibleWebClient)
    .url("/users/123")
    .build()
    .executeAsync(UserResponse::class.java)
```

이제 Kotlin에서 이 라이브러리를 훨씬 더 안전하고 편리하게 사용할 수 있습니다! 🎉