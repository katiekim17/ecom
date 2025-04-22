# 동시성 제어를 위한 락 설정 보고서

동시성 이슈를 고민하던 도중, 특정 값이 이미 영속화가 되어 있고, 해당 값을 기준으로 새로운 값을 생성하거나, 수정하는 경우에 

동시성 이슈가 발생할 수 있는 상태가 된다는 생각을 하게 되었다.

검색을 해보니 Read-Modify-Write 패턴이라고 한다. 이는 경쟁 상태의 패턴이라고 하는데, 또 다른 패턴으로는 Check-Then-Act가 있다.

하지만 그렇게 된다면 대부분의 로직에 동시성 제어 코드가 추가되어야 할텐데, 이는 어떻게 해야 하는지 궁금하긴 하다.

우선 대표적으로 동시성 이슈가 발생하는 로직은 아래 3가지라고 볼 수 있다.

1. 선착순 쿠폰 발급
2. 사용자 포인트 충전 및 사용
3. 주문 시 상품 재고 차감

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


## 포인트 충전 및 사용

포인트 충전과 사용의 경우 충돌 가능성이 매우 적지만, 아이디를 공유하여 결제를 진행하는 경우도 있을 수 있다.

비즈니스적으로 충전의 경우에는 실패하여도 리스크가 적다고 판단되기 때문에 낙관적 락을 적용하고,

사용의 경우에는 비관적 락을 적용해보도록 하려고 한다.

### 포인트 충전 낙관적 락 적용

현재 포인트 충전의 테스트 코드는 아래와 같으며, 10번 다 성공처리되어 100 포인트를 기대하였지만 40포인트만 저장되어 있다.

```java
    @DisplayName("동시에 여러번 충전을 진행하여도, 충전을 성공한 만큼만 포인트가 추가된다.")
    @Test
    @Sql(scripts = "/sql/point.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void chargeConcurrency() throws InterruptedException{
        // given
        Long userId = 1L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        int chargeAmount = 10;
        AtomicInteger successCnt = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    pointService.charge(new PointCommand.Charge(userId, chargeAmount));
                    successCnt.getAndIncrement();
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 작업이 끝날 때까지 대기

        // then
        Point finalPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(finalPoint.getBalance()).isEqualTo(chargeAmount * successCnt.get()); // 총합 결과
    }
```

Point Entity에 낙관적 락을 적용하기 위해 Version을 세팅, charge 메서드에 재시도 로직도 포함하였다.

```java
@Getter
@Entity
@NoArgsConstructor
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int balance;

    @Version
    private Long version;
}
```

```java
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)      // 100ms → 200ms → 400ms
    )
    @Transactional
    public Point charge(PointCommand.Charge command) {

        Point point = find(command.userId());
        point.charge(command.amount());

        return point;
    }
```

기존의 테스트코드가 통과되는 것으로 보아 정상적으로 낙관적 락이 적용되었음을 확인할 수 있다.

### 포인트 사용 비관적 락 적용

포인트 사용 테스트는 아래와 같으며, 10번의 시도를 진행하면 10번 모두 사용되어 잔여 포인트가 0이 되는 것을 기대하고 있다.

```java
    @DisplayName("동시에 사용을 여러번 하여도 포인트가 충분한 경우 모두 사용되며, 포인트 차감이 정상 적용된다.")
    @Test
    void concurrencyUseAndCharge() throws InterruptedException {
        // given
        User user = jpaUserRepository.save(User.create("user"));
        jpaPointRepository.save(Point.create(user, 100));

        Long userId = user.getId();

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    pointService.use(new PointCommand.Use(userId, 10));
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 작업이 끝날 때까지 대기

        // then
        Point finalPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(finalPoint.getBalance()).isEqualTo(0); // 총합 결과
    }
```

결과는 40포인트만 사용되었으며, 낙관적 락을 Entity Point에 걸어두었기 때문에, 재시도는 진행되지 않은 채 ObjectOptimisticLockingFailureException만 발생되고 있다.

```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.userId = :userId")
    Optional<Point> findByUserIdForUpdate(Long userId);
```

포인트 사용할 때 포인트를 조회하는 쿼리에 비관적 락을 적용하여 해당 테스트를 통과시켰다.

낙관적 락 (version 관리)가 적용된 Entity여도 비관적 lock이 적용되는 경우 수정될 때 항상 version이 동일하므로 전혀 문제가 되지 않는 모습을 확인할 수 있었다.


## 주문 시 상품 재고 차감

