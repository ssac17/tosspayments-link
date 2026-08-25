# Toss Payments Link

**결제 게이트웨이 결제 처리 및 관리 시스템**

Spring Boot 기반의 엔터프라이즈급 RESTful API 서버입니다. Toss Payments API를 연동하여 **안전하고 확장 가능한 결제 처리**, **실시간 모니터링**, **동시성 제어**를 구현했습니다.

🔗 **실제 배포 주소**: [https://tosspayments-link.onrender.com](https://tosspayments-link.onrender.com)

## 프로젝트 개요

이 프로젝트는 **실제 프로덕션 환경에서의 결제 처리**를 다루는 엔터프라이즈급 시스템입니다. 결제 확인/취소, 실시간 대시보드, 재고 관리 등 결제 시스템의 모든 핵심 요소를 포함하고 있습니다.

### 🎯 핵심 기능

| 기능 | 설명 |
|------|------|
| **결제 승인/취소** | Toss Payments API 연동, Basic Auth 기반 보안 |
| **실시간 대시보드** | WebSocket/STOMP 기반 양방향 통신 |
| **결제 내역 관리** | 페이지네이션, 다양한 필터 및 검색 |
| **동시성 제어** | Named Lock을 통한 재고 관리의 데이터 일관성 보장 |
| **멱등성 보장** | 중복 결제/취소 방지 |
| **상품 관리** | 재고 추적 및 자동 감소 처리 |

### 💻 기술 스택

| 카테고리 | 기술 |
|---------|------|
| **Framework** | Spring Boot 4.1.0, Spring Data JPA |
| **Language** | Java 25 (최신 언어 기능 활용) |
| **Database** | MySQL 8.0+, JPA/Hibernate ORM |
| **통신** | WebSocket/STOMP (실시간 양방향 통신) |
| **HTTP Client** | Spring Web RestClient (선언적 API 호출) |
| **Build Tool** | Gradle 8.0+ |
| **라이브러리** | Lombok, JSON-Simple |
| **Deployment** | Docker, Render (배포 프라파이프라인)

## 아키텍처

### 계층별 설계

```
src/main/java/com/toss/tosspaymentslink/
│
├── controller/                      # REST API 엔드포인트
│   ├── PayController               # 결제 API (확인, 취소, 조회)
│   ├── ProductController           # 상품 API (조회)
│   └── HomeController              # 헬스체크
│
├── service/                         # 핵심 비즈니스 로직
│   ├── PayService                  # 결제 처리 및 관리
│   │   ├── 결제 승인 (Toss API 연동)
│   │   ├── 결제 취소 (멱등성 보장)
│   │   ├── 재고 감소 (Named Lock)
│   │   └── 실시간 알림 (WebSocket)
│   │
│   └── ProductService              # 상품 관리
│       └── 상품 조회, 재고 업데이트
│
├── domain/                          # 엔티티 모델
│   ├── entity/
│   │   ├── Payment                  # 결제 정보
│   │   │   ├── paymentKey (PK)
│   │   │   ├── orderId (Unique)
│   │   │   ├── status (DONE/WAITING/FAILED/CANCELED)
│   │   │   ├── card (Embedded)
│   │   │   ├── cancels (OneToMany)
│   │   │   └── 기타 결제 세부정보
│   │   │
│   │   ├── Product                  # 상품 정보
│   │   │   ├── id, name, price
│   │   │   ├── stock (낙관적 잠금)
│   │   │   └── 기타 상품 정보
│   │   │
│   │   ├── Cancels                  # 결제 취소 이력
│   │   └── CashReceipts             # 현금 영수증
│   │
│   ├── embedded/                    # 임베디드 타입
│   │   ├── Card                     # 카드 정보
│   │   ├── Refund                   # 환불 정보
│   │   └── CashReceipt              # 현금영수증
│   │
│   └── enums/
│       └── PaymentStatus            # DONE, WAITING, FAILED, CANCELED
│
├── dto/                             # Data Transfer Objects
│   ├── PaymentConfirmRequestDto     # 결제 승인 요청
│   ├── PaymentCancelRequestDto      # 결제 취소 요청
│   ├── PaymentResponseDto           # 결제 응답
│   ├── AdminNotificationDto         # 관리자 실시간 알림
│   └── PageResponseDto              # 페이지네이션 응답
│
├── repository/                      # Data Access Layer
│   ├── PayRepository                # 결제 데이터 저장소
│   │   ├── findByPaymentKey()
│   │   ├── findByOrderId()
│   │   ├── findAllWithPagination()
│   │   └── decreaseStockWithLock() [Named Lock]
│   │
│   └── ProductRepository            # 상품 데이터 저장소
│       └── Custom Query Methods
│
├── config/                          # 애플리케이션 설정
│   ├── WebSocketConfig              # STOMP 엔드포인트 설정
│   ├── Config                       # RestClient Bean 등록
│   └── DataInitializer              # CSV 기반 초기 데이터 로딩
│
└── exception/                       # 예외 처리
    └── Global Error Handler
```

### 🔄 결제 처리 흐름

```
클라이언트 결제 요청
        │
        ▼
[1] PayController.confirm()
        │
        ├─→ [2] 상품 정보 검증
        │
        ├─→ [3] Toss Payments API 호출
        │        (PaymentKey + OrderId + Amount)
        │
        ├─→ [4] 결제 결과 저장 (DB)
        │        Payment entity INSERT
        │
        ├─→ [5] 재고 감소 (Named Lock)
        │        Product.stock 낙관적 잠금
        │
        └─→ [6] 실시간 알림 (WebSocket)
                 SimpMessagingTemplate.convertAndSend()
                        │
                        ▼
                 [관리자 대시보드]
                 /topic/admin/payment 구독자에게 전송
```

### 🛡️ 보안 및 동시성 제어

| 요소 | 구현 | 목적 |
|------|------|------|
| **Toss API 인증** | Basic Auth (Secret Key) | API 호출 보안 |
| **멱등성** | Idempotency-Key 사용 | 중복 결제/취소 방지 |
| **동시성 제어** | Named Lock (JPA @Lock) | 재고 감소 시 데이터 일관성 |
| **트랜잭션** | @Transactional | ACID 특성 보장 |
| **데이터 검증** | DTO Validation | 입력값 무결성 |

## 주요 기능 상세

### 1️⃣ 결제 처리 API

#### 1-1. 결제 승인 (`POST /v1/payments/confirm`)

```http
POST https://tosspayments-link.onrender.com/v1/payments/confirm
Content-Type: application/json

{
  "paymentKey": "toss_12345678910",
  "orderId": "ORDER_2024_001",
  "amount": 50000,
  "productId": 1,
  "name": "노트북"
}
```

**응답**:
```json
{
  "paymentKey": "toss_12345678910",
  "orderId": "ORDER_2024_001",
  "amount": 50000,
  "status": "DONE",
  "approvedAt": "2024-08-25T10:30:00+09:00",
  "card": {
    "cardCompany": "신한카드",
    "cardNumber": "****1234"
  },
  "product": {
    "id": 1,
    "name": "노트북",
    "price": 50000
  }
}
```

**처리 흐름**:
1. **상품 검증**: 상품 존재 여부 및 재고 확인
2. **Toss API 호출**: 결제 승인 요청 (Basic Auth)
3. **DB 저장**: Payment 엔티티 저장
4. **재고 감소**: Product.stock 감소 (Named Lock)
5. **실시간 알림**: 관리자 대시보드에 WebSocket 전송

---

#### 1-2. 결제 내역 조회 (`GET /v1/payments`)

```http
GET https://tosspayments-link.onrender.com/v1/payments?page=0&size=5
```

**특징**:
- 페이지네이션 (Pageable)
- 최신 승인일시 기준 내림차순 정렬
- 결제 상태별 필터링

**응답**:
```json
{
  "content": [
    {
      "paymentKey": "toss_12345678910",
      "orderId": "ORDER_2024_001",
      "amount": 50000,
      "status": "DONE",
      "approvedAt": "2024-08-25T10:30:00+09:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "totalElements": 100,
    "totalPages": 20
  }
}
```

---

#### 1-3. 단건 조회

```http
GET /v1/payments/{paymentKey}
GET /v1/payments/orders/{orderId}
```

---

#### 1-4. 결제 취소 (`POST /v1/payments/{paymentKey}/cancel`)

```http
POST https://tosspayments-link.onrender.com/v1/payments/toss_12345678910/cancel
Content-Type: application/json

{
  "cancelReason": "상품 품절"
}
```

**주요 특징**:
- ✅ 멱등키(Idempotency-Key) 사용으로 중복 취소 방지
- ✅ 이미 취소된 결제건 재취소 방지
- ✅ 취소 정보를 별도 Cancels 엔티티로 저장
- ✅ 환불액 자동 계산
- ✅ 실시간 대시보드 업데이트

---

### 2️⃣ 실시간 대시보드 (WebSocket)

**WebSocket Endpoint**: `ws://tosspayments-link.onrender.com/ws-endpoint`

**STOMP 구독**:
```
/topic/admin/payment
```

**실시간 이벤트**:
```json
{
  "type": "PAYMENT_CONFIRMED",
  "paymentKey": "toss_12345678910",
  "orderId": "ORDER_2024_001",
  "amount": 50000,
  "productName": "노트북",
  "timestamp": "2024-08-25T10:30:00+09:00"
}
```

**기능**:
- 결제 성공/취소 이벤트 실시간 전송
- 관리자 화면에서 즉각적인 데이터 반영
- STOMP 프로토콜 기반 효율적 메시지 전달

---

### 3️⃣ 상품 관리 (`/v1/products`)

```http
GET https://tosspayments-link.onrender.com/v1/products/{productId}
```

**데이터 구조**:
```json
{
  "id": 1,
  "name": "노트북",
  "price": 50000,
  "stock": 95,
  "description": "고성능 노트북"
}
```

**기능**:
- 상품 정보 조회
- 재고 자동 추적
- 결제 성공 시 재고 자동 감소

---

## 📋 API 엔드포인트

| Method | Endpoint | Description | 비고 |
|--------|----------|-------------|------|
| `POST` | `/v1/payments/confirm` | 결제 승인 | Toss API 연동 |
| `GET` | `/v1/payments` | 결제 내역 조회 (페이지네이션) | 정렬, 필터링 |
| `GET` | `/v1/payments/{paymentKey}` | 결제 단건 조회 | - |
| `GET` | `/v1/payments/orders/{orderId}` | 주문ID로 결제 조회 | - |
| `POST` | `/v1/payments/{paymentKey}/cancel` | 결제 취소 | 멱등성 보장 |
| `GET` | `/v1/products/{productId}` | 상품 정보 조회 | 재고 포함 |
| `GET` | `/health` | 헬스체크 | - |

---

## 🚀 빠른 시작

### 필수 요구사항
- **JDK 25+** (최신 Java 언어 기능)
- **MySQL 8.0+** (InnoDB 엔진)
- **Gradle 8.0+**
- **Render** (배포 플랫폼)

### 환경 설정

`application.yaml` 또는 환경 변수 설정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tosspayments_db
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

widget:
  secret:
    key: ${TOSS_SECRET_KEY}  # 환경 변수에서 주입
```

### 빌드 및 실행

```bash
# 1. 프로젝트 클론
git clone https://github.com/ssac17/tosspayments-link.git
cd tosspayments-link

# 2. 빌드
./gradlew clean build

# 3. 실행 (로컬)
./gradlew bootRun

# 4. JAR 실행
java -jar build/libs/tosspayments-link-0.0.1-SNAPSHOT.jar
```

**로컬 실행 주소**: `http://localhost:8080`

### 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests PayServiceTest

# 테스트 리포트 생성
./gradlew test --info
```

---

## 🏗️ 주요 기술 구현

### 1. 동시성 제어 (Concurrency Control)

**문제**: 여러 요청이 동시에 재고를 감소시킬 때 데이터 불일치 발생

**해결**: **Named Lock** (비관적 잠금)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :productId")
Optional<Product> findByIdForUpdate(@Param("productId") Long productId);
```

**장점**:
- 데이터 일관성 보장
- Race condition 방지
- 낙관적 잠금보다 안정적

---

### 2. 실시간 통신 (WebSocket/STOMP)

**기술**: Spring WebSocket + STOMP 프로토콜

**설정**:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

**메시지 전송**:
```java
simpMessagingTemplate.convertAndSend("/topic/admin/payment", notificationDto);
```

---

### 3. API 보안 (Toss Payments 연동)

**인증 방식**: Basic Authentication

```java
RestClient restClient = RestClient.builder()
    .defaultHeader(HttpHeaders.AUTHORIZATION, 
        "Basic " + Base64.getEncoder().encodeToString(
            (secretKey + ":").getBytes()))
    .baseUrl(TOSS_API_URL)
    .build();
```

---

### 4. 멱등성 보장 (Idempotency)

**목적**: 중복 요청으로 인한 중복 결제/취소 방지

**구현**: Idempotency-Key를 이용한 요청 추적

```java
@PostMapping("/{paymentKey}/cancel")
public ResponseEntity<PaymentResponseDto> cancelPayment(
    @PathVariable String paymentKey,
    @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
    @RequestBody PaymentCancelRequestDto request) {
    // 동일한 idempotencyKey로의 중복 요청 감지 및 처리
}
```

---

### 5. 데이터 초기화 (CSV 기반)

**파일**: `src/main/resources/products.csv`

**자동 로딩**: DataInitializer 클래스

```java
@Component
public class DataInitializer implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        // products.csv 파일에서 상품 데이터 로딩
    }
}
```

---

## 📊 성능 최적화

### 1. 데이터베이스
- **JPA Named Query** 활용
- **페이지네이션**으로 메모리 효율화
- **Index 설정** (paymentKey, orderId)
- **Lazy Loading** 전략

### 2. 네트워크
- **DTO 활용**으로 필드 선택적 직렬화
- **API 응답 최소화**
- **압축** (gzip)

### 3. 애플리케이션
- **Connection Pool** (HikariCP)
- **쿼리 최적화**
- **캐싱 전략** (가능 시 Redis 적용)

---

## 🎓 이 프로젝트에서 배운 점

| 학습 항목 | 내용 |
|----------|------|
| **Spring Data JPA** | 낙관적/비관적 잠금을 통한 동시성 제어 |
| **WebSocket** | 실시간 양방향 통신 구현 및 STOMP 프로토콜 활용 |
| **REST API 설계** | 멱등성과 상태 관리의 중요성 |
| **트랜잭션 관리** | 결제 시스템에서의 ACID 특성 보장 |
| **API 통합** | 외부 결제 게이트웨이와의 안전한 연동 |
| **보안** | Basic Auth, 데이터 암호화, 입력값 검증 |
| **배포** | Docker & Render를 통한 클라우드 배포 |

---

## 🔍 프로덕션 체크리스트

- ✅ 결제 API Basic Auth 보안 구현
- ✅ 멱등성 보장 (중복 요청 방지)
- ✅ 동시성 제어 (Named Lock)
- ✅ 트랜잭션 관리 (ACID)
- ✅ 실시간 대시보드 (WebSocket)
- ✅ 에러 핸들링 및 로깅
- ✅ 페이지네이션 및 쿼리 최적화
- ✅ Docker 이미지 빌드
- ✅ Render 배포
- ⚠️ Redis 캐싱 (향후 개선)
- ⚠️ Swagger/OpenAPI 문서화 (향후 개선)

---

### 필수 요구사항
- JDK 25 이상
- MySQL 8.0 이상
- Gradle 8.0 이상

### 환경 설정

`application.yaml` 설정:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tosspayments_db
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

widget:
  secret:
    key: your_toss_secret_key
```

### 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 또는
java -jar build/libs/tosspayments-link-0.0.1-SNAPSHOT.jar
```

서버는 `http://localhost:8080`에서 실행됩니다.


## 향후 개선사항

### 우선순위 높음
- [ ] **Redis 캐싱**: 반복적인 상품/결제 조회 캐싱
- [ ] **API 문서**: Swagger/OpenAPI 자동화
- [ ] **결제 실패 처리**: 재시도 로직 및 보상 트랜잭션

### 우선순위 중간
- [ ] **고급 검색**: 결제 내역 다중 필터링
- [ ] **관리자 대시보드**: 통계 및 분석 기능
- [ ] **로깅**: ELK Stack 또는 CloudWatch 통합

### 우선순위 낮음
- [ ] 테스트 커버리지 100% 달성
- [ ] 결제 모니터링 알림 시스템
- [ ] 다중 통화 지원

---

## 📚 참고 자료

- [Toss Payments API 문서](https://docs.tosspayments.com/)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Data JPA 동시성 제어](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.locking)
- [STOMP 프로토콜 가이드](https://stomp.github.io/)

---

## 📧 연락처

이 프로젝트에 대한 질문이나 제안이 있으신가요?

- **GitHub Issues**: [이슈 등록하기](https://github.com/ssac17/tosspayments-link/issues)
- **이메일**: 협력 및 피드백 환영합니다.

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

<div align="center">

**🌟 이 프로젝트가 도움이 되었다면 Star를 눌러주세요! 🌟**

[배포 서버 방문하기](https://tosspayments-link.onrender.com)

</div>
