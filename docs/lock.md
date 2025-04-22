# 동시성 제어를 위한 락 설정 보고서

동시성 이슈가 발생하는 로직은 아래 3가지이다.

1. 선착순 쿠폰 발급
2. 주문 시 상품 재고 차감
3. 사용자 포인트 충전 및 사용

위 3가지 케이스에 대해 Lock을 활용해 동시성 제어를 진행해보고자 한다.

## 선착순 쿠폰 발급 API

TC.
1. 쿠폰의 남은 수량이 10개일 때, 10명의 사용자가 발급을 동시에 요청하면 모두 발급되고 남은 수량이 0개가 된다.

```java
    @DisplayName("쿠폰의 수량이 10개 남았을 때, 10명의 사용자가 발급을 동시에 요청하면 모두 발급되고 쿠폰의 수량이 0개 남는다.")
    @Test
    @Commit
    @Sql(scripts = "/sql/userCoupon.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void issueConcurrency() throws InterruptedException{
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long couponId = 1L;

        // when
        for (long i = 1; i <= threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    CouponCriteria.IssueUserCoupon criteria = new CouponCriteria.IssueUserCoupon(userId, couponId);
                    couponFacade.issueUserCoupon(criteria);
                } catch (Exception e) {
                    log.error("{}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 작업이 끝날 때까지 대기

        // then
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        List<UserCoupon> all = jpaUserCouponRepository.findAll();
        assertThat(all).hasSize(10); // has 10 !
        assertThat(coupon.getQuantity()).isEqualTo(0); // quantity = 6!
    }
```

아무런 Lock을 걸지 않은 상태에서 해당 테스트는 발급에는 모두 성공하였지만, 남은 수량이 6개였다.

해당 테스트 케이스를 통과하기 위해 낙관적 락과 비관적 락을 모두 적용해서 테스트를 진행해보고자 한다.

### 낙관적 락 적용

낙관적 락은 lock을 걸지 않고, 버전 관리를 통해 충돌여부를 확인합니다.

적용 방법은 낙관적 락을 사용하려는 Entity에 아래와 같은 version을 세팅합니다.

```java
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;
}
```

해당 필드 명은 변경할 수 있으며, 이는 Spring Boot에서 낙관적 락을 보다 편리하게 적용할 수 있게 제공해주는 라이브러리입니다.

```java
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public UserCoupon issue(UserCouponCommand.Issue command){
    
    }
```

쿠폰을 발급해주는 메서드에 재시도 어노테이션을 붙여주어서 충돌 발생 시 재시도를 진행하게 수행하였습니다.

이때의 테스트 코드는 다음과 같이 구성됩니다.

```java
    @DisplayName("쿠폰의 수량이 10개 남았을 때, 10명의 사용자가 발급을 동시에 요청하면 발급된 수 만큼만 쿠폰의 수량이 차감된다.")
    @Test
    @Sql(scripts = "/sql/userCoupon.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void issueConcurrency() throws InterruptedException{
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long couponId = 1L;

        // 쿠폰 발급 수행

        // then
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        List<UserCoupon> issuedCoupons = jpaUserCouponRepository.findAll();
        assertThat(coupon.getQuantity()).isEqualTo(coupon.getInitialQuantity() - issuedCoupons.size());
    }
```

낙관적 락에선 충돌이 발생했을 경우 delay를 가지고 재시도를 진행하기 때문에, 그 중간에 새로운 Transaction이 실행되어 충돌 없이 수행된다면

먼저 쿠폰 발급을 시도한 유저는 실패하고, 이후에 시도한 유저는 성공하는 공정성 문제가 발생하게 됩니다.

### 비관적 락 적용

비관적 락은 LockModeType를 통해 3가지 락 관리를 제공합니다. 

| LockModeType                | 적용 효과                           |
|-----------------------------|---------------------------------|
| PESSIMISTIC_WRITE           | 동시에 읽기/쓰기 모두 차단해 Lost Update 방지 |
| PESSIMISTIC_READ            | 조회 시점 고정, 이후 재고 변경 방지(일관된 읽기)   |
| PESSIMISTIC_FORCE_INCREMENT | 버전 충돌 감지 + 즉시 버전 증가로 재시도 제어     |

따라서 PESSIMISTIC_WRITE를 적용하지만, 좀 더 효율적인 lock 관리를 위해 validtion과 deduct를 분리하였습니다.


```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdForUpdate(Long id);
```
deduct 시 coupon을 조회할 때 PESSIMISTIC_WRITE를 통한 비관적 락을 적용합니다.

```java
class CouponFacade {
    @Transactional
    public CouponResult.IssueUserCoupon issueUserCoupon(CouponCriteria.IssueUserCoupon criteria) {

        User user = userService.findById(criteria.userId()); 
        Coupon coupon = couponService.issueValidate(criteria.couponId()); // lock 미적용
        UserCoupon userCoupon = userCouponService.issue(criteria.toCommand(user, coupon));
        couponService.deduct(criteria.couponId()); // lock 적용

        return CouponResult.IssueUserCoupon.from(userCoupon);
    }
}
```
validate를 진행할 때에는 lock이 걸리지 않은 findById메서드로 coupon을 조회하여 validation을 진행

deduct 로직에서 findByIdForUpdate 메서드로 lock이 걸린 coupon 조회 query를 사용하여 lock 관리를 효율적으로 진행하였습니다.


