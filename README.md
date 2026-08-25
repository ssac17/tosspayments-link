# Toss Payments Link

**Toss Payments API를 활용한 결제 시스템 백엔드 애플리케이션**

Spring Boot 기반의 RESTful API 서버로, Toss Payments(토스페이먼츠) 결제 게이트웨이를 연동하여 안전한 결제 처리와 실시간 결제 현황 모니터링을 제공합니다.

## 프로젝트 개요

### 핵심 기능
- **결제 승인/취소**: Toss Payments API를 통한 안전한 결제 처리 및 취소
- **결제 내역 관리**: 결제 데이터 저장, 조회, 페이지네이션
- **실시간 대시보드**: WebSocket 기반 관리자 화면에 결제 정보 실시간 전송
- **재고 관리**: 결제 성공 시 자동 재고 감소 처리
- **상품 관리**: 상품 정보 및 가격 조회

### 기술 스택
- **Framework**: Spring Boot 4.1.0
- **Language**: Java 25
- **Database**: MySQL + Spring Data JPA
- **Communication**: WebSocket (STOMP)
- **API Client**: Spring Web RestClient
- **Build Tool**: Gradle
- **Additional**: Lombok, json-simple

## 아키텍처

```
src/main/java/com/toss/tosspaymentslink/
├── contorller/                  # REST API 엔드포인트
│   ├── PayController           # 결제 API 컨트롤러
│   ├── ProductController       # 상품 API 컨트롤러
│   └── HomeController          # 홈 페이지 컨트롤러
├── service/                     # 비즈니스 로직
│   ├── PayService             # 결제 처리 서비스
│   └── ProductService         # 상품 관리 서비스
├── domain/
│   ├── entity/                # JPA 엔티티
│   │   ├── Payment            # 결제 정보
│   │   ├── Product            # 상품 정보
│   │   ├── Cancels            # 결제 취소 정보
│   │   └── CashReceipts       # 현금 영수증
│   ├── embeded/               # 임베디드 타입
│   └── enums/                 # 열거형
├── dto/                         # Data Transfer Objects
│   ├── PaymentConfirmRequestDto  # 결제 승인 요청
│   ├── PaymentCancelRequestDto   # 결제 취소 요청
│   ├── PaymentResponseDto        # 결제 응답
│   └── AdminNotificationDto      # 관리자 알림
├── repository/                  # Data Access Layer
│   ├── PayRepository          # 결제 데이터 저장소
│   └── ProductRepository      # 상품 데이터 저장소
└── config/                      # 설정
    ├── WebSocketConfig        # WebSocket 설정
    ├── Config                 # 전역 설정
    └── DataInitializer        # 초기 데이터 로딩
```

## 주요 기능 상세

### 1. 결제 처리 (`/v1/payments`)

#### 결제 승인
```http
POST /v1/payments/confirm
Content-Type: application/json

{
  "paymentKey": "string",
  "orderId": "string",
  "amount": 10000,
  "productId": 1,
  "name": "상품명"
}
```

**처리 흐름**:
1. 상품 정보 검증
2. Toss Payments API에 결제 승인 요청 (Basic Auth)
3. 결제 내역을 DB에 저장
4. 상품 재고 감소 (낙관적 잠금)
5. WebSocket을 통해 관리자 대시보드에 실시간 알림 전송

#### 결제 내역 조회
```http
GET /v1/payments?page=0&size=5
```
- 페이지네이션 기반 조회
- 최신 승인일시 기준 내림차순 정렬

#### 단건 조회
```http
GET /v1/payments/{paymentKey}
GET /v1/payments/orders/{orderId}
```

#### 결제 취소
```http
POST /v1/payments/{paymentKey}/cancel
Content-Type: application/json

{
  "cancelReason": "상품 품절"
}
```

**주요 특징**:
- 멱등키(Idempotency-Key) 사용으로 중복 취소 방지
- 이미 취소된 결제건 재취소 방지
- 취소 정보를 별도 엔티티로 저장
- 실시간 대시보드 업데이트

### 2. 실시간 대시보드

**WebSocket Endpoint**: `/ws-endpoint`

**Subscribe Topics**:
```
/topic/admin/payment  # 결제/취소 알림 수신
```

**기능**:
- 결제 성공/취소 이벤트 실시간 전송
- 관리자 화면에서 즉각적인 업데이트
- STOMP 프로토콜 기반 메시지 전달

### 3. 결제 데이터 구조

#### Payment 엔티티
```java
- paymentKey: String (PK)
- orderId: String (고유)
- amount: Long
- status: PaymentStatus (DONE, WAITING, FAILED, CANCELED)
- approvedAt: OffsetDateTime
- product: Product
- cancels: List<Cancels>
```

#### 임베디드 타입
- Card 정보 (카드사, 카드 번호 마지막 4자리 등)
- Refund 정보 (환불 금액, 취소 사유)
- CashReceipt 정보 (현금영수증)

### 4. 보안 및 최적화

**보안**:
- Toss Payments API 인증: Basic Auth (Secret Key 암호화)
- JPA Named Lock으로 동시성 제어 (재고 감소)

**최적화**:
- 낙관적 잠금으로 재고 관리
- API 응답 캐싱
- JPA 쿼리 최적화

## 설치 및 실행

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

## API 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/payments/confirm` | 결제 승인 |
| GET | `/v1/payments` | 결제 내역 조회 (페이지네이션) |
| GET | `/v1/payments/{paymentKey}` | 결제 단건 조회 |
| GET | `/v1/payments/orders/{orderId}` | 주문ID로 결제 조회 |
| POST | `/v1/payments/{paymentKey}/cancel` | 결제 취소 |
| GET | `/v1/payments/today-summary` | 오늘의 결제 통계 |

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests PayServiceTest
```

## 주요 개발 사항

### 동시성 제어
- **Named Lock**: Product 엔티티의 재고 감소 시 데이터 일관성 보장
- **Transactional**: 결제 승인/취소 시 원자성(Atomicity) 보장

### 실시간 통신
- **WebSocket/STOMP**: 클라이언트와 양방향 실시간 통신
- **SimpMessagingTemplate**: 특정 구독자에게 메시지 전송

### API 연동
- **RestClient**: Spring Web의 선언적 HTTP 클라이언트
- **Exception Handling**: Toss API 오류 응답 파싱 및 전달

### 데이터 설계
- **CSV 기반 초기화**: `products.csv`에서 상품 데이터 자동 로딩
- **Embedded Types**: 결제 관련 복합 데이터 구조화

## 성능 최적화

1. **데이터베이스**
   - JPA Named Query 활용
   - 페이지네이션으로 메모리 효율화
   - Index 설정 (paymentKey, orderId)

2. **네트워크**
   - API 응답 타입별 최소 데이터 전송
   - DTO를 통한 선택적 필드 직렬화

## 배운 점

- **Spring Data JPA**: 낙관적/비관적 잠금을 통한 동시성 제어
- **WebSocket**: 실시간 양방향 통신 구현
- **REST API 설계**: 멱등성과 상태 관리의 중요성
- **트랜잭션 관리**: 결제 시스템에서의 ACID 보장
- **API 통합**: 외부 결제 게이트웨이와의 안전한 연동

## 향후 개선사항

- [ ] 결제 재시도 로직 및 실패 처리 개선
- [ ] 결제 내역 조회 검색 필터 추가
- [ ] 관리자 대시보드 기능 확장
- [ ] Redis 캐싱 적용
- [ ] API 문서 자동화 (Swagger/OpenAPI)
- [ ] 단위 테스트 커버리지 확대
- [ ] Docker 컨테이너화


## 연락처

질문이나 피드백은 이슈로 등록해주세요.

