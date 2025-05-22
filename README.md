STEP 16 Transaction Diagnosis
과제 : 우리 서비스의 규모가 확장되어 MSA의 형태로 각 도메인별로 배포 단위를 분리해야한다면 각각 어떤 도메인으로 배포 단위를 설계할 것인지 결정하고, 그 분리에 따른 트랜잭션 처리의 한계와 해결방안에 대한 서비스 설계 문서 작성하여 제출합니다.

MSA 전환 설계 문서: OrderFacade 기반 이커머스 서비스 분리 설계안

1. 도메인 분석 및 서비스 분리 기준
   현재 도메인 구조
   OrderFacade는 다음의 도메인에 의존합니다
    - UserService: 유저 정보, 쿠폰 사용 여부 확인
    - ProductService: 상품 재고 확인 및 차감
    - OrderService: 주문 정보 생성
    - PaymentService: 결제 처리
    - UserCouponService: 쿠폰 검증 및 사용 처리

2. MSA로 전환 시 서비스 분리 리스트
   도메인                      책임                      마이크로서비스 이름
   User                 유저 인증, 정보 조회      user-service
   Product              상품 정보, 재고 차감      product-service
   Order                주문 생성, 조회          order-service
   Payment              결제 처리 및 상태 관리     payment-service
   Coupon               쿠폰 검증, 사용          coupon-service
   Notification/Event   주문 완료 이벤트 처리 등   notification-service (또는 async consumer)

3. 트랜잭션 처리의 한계점
   문제: 분산 트랜잭션 발생
   OrderFacade는 단일 트랜잭션 내에서 여러 도메인의 상태를 변경함. MSA에서는 로컬 트랜잭션만 존재하므로, 전체 흐름의 ACID 보장 불가
   - 재고 차감 성공, 주문 생성 성공, 결제 실패 → 재고 복구 필요
   - 쿠폰 사용 성공, 주문 생성 실패 → 쿠폰 롤백 필요

4. 해결 방안
    A. SAGA 패턴 (권장)
    SAGA - Choreography 기반 예시
    order-service가 주문 생성 및 OrderCreatedEvent 발행
    payment-service가 이를 consume하고 결제 처리 → 성공 시 PaymentCompletedEvent 발행
    product-service는 재고 차감 수행
    coupon-service는 쿠폰 사용 처리
    실패 시 각 서비스가 Compensation Command (보상 작업)를 수행
    장점: 서비스 간 의존도 낮고, 이벤트 기반 확장 용이
    단점: 이벤트 순서 및 처리 상태 관리 복잡

    B. Orchestration 기반 SAGA
    order-service가 모든 작업을 orchestrate
    각 서비스는 Command API를 제공하고, 상태 결과를 callback 또는 상태 저장으로 응답
    실패 시 중앙에서 각 서비스에 보상 요청 전송

    C. Outbox Pattern + Polling Consumer
    이벤트 발행 실패 시를 대비하여 로컬 DB에 이벤트 기록 후 비동기 전송
    Outbox 테이블 → 메시지 브로커 → 소비자 서비스

5. 트랜잭션 예외 시 처리 방안 (보상 전략)
   단계	            실패 상황	                    보상 처리 방법
   결제	            결제 실패	            주문 취소, 쿠폰 롤백, 재고 복원
   재고 차감	        재고 부족	            주문 실패 처리, 쿠폰 롤백
   쿠폰 사용	    쿠폰 만료/사용 불가	        주문 실패 처리, 재고 복원

6. 결론
   MSA 환경에서는 OrderFacade처럼 복합 도메인을 한 번에 처리하는 방식이 불가능해지므로, 
   이벤트 기반의 SAGA 패턴이나 Orchestration 기반의 보상 트랜잭션을 도입해야 한다. 서비스 간의 경계를 명확히 분리하고, 
   각 서비스는 독립적으로 트랜잭션을 수행하고, 실패를 보상 처리할 수 있어야 안정적인 이커머스 서비스가 가능하다.