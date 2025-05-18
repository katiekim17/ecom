# 쿠폰 발급 비동기로 성능 개선

현재 쿠폰 로직은 다음과 같습니다.

```java
    @Transactional
    @DistributedLock(key = "'lock:coupon:' + #id", type = LockType.FAIR)
    public CouponResult.IssueUserCoupon issueUserCoupon(CouponCriteria.IssueUserCoupon criteria) {

        Coupon coupon = couponService.issueValidate(criteria.couponId());
        UserCoupon userCoupon = userCouponService.issue(criteria.toCommand(coupon));
        couponService.deduct(criteria.couponId());

        return CouponResult.IssueUserCoupon.from(userCoupon);
    }
```

### 현재 문제점

선착순 쿠폰 발급 특성상 많은 요청이 한 번에 들어와 분산락을 적용하여 동시성 이슈를 해결하였지만, DB 데이터를 update 및 insert하는 작업이 다이렉트로 이루어져 짧은 시간내에 많은 트래픽이 발생했을 때 DB 병목 현상 및 서버 부하를 발생시킬 수 있습니다.

### 해결 방안

해당 기능은 요청은 빠르게 응답하고, 실제 발급된 쿠폰의 사용은 요청과는 다른 시간대에 발생하는 케이스입니다.

이에 따라 많은 트래픽이 발생하는 요청 부분을 Redis를 통해 Queue를 만들어 요청만 적재하고,

해당 Queue 데이터를 기반하여 Scheduler가 쿠폰을 발급하는 형태로 구성하고자 합니다.

### 이점

부하가 발생하는 요청부분에서 API 싸이클을 좁게 가져가고, DB부하를 감소시킴으로써 Server의 부하도 감소시킵니다.

### 문제점

실시간으로 발급이 되지 않아 유저의 사용성이 불편해집니다.

실제로 요청을 진행하였지만 발급이 되지 않는 사례에 대해 별도의 폴링 API를 제공하거나, 알림을 구현해야 합니다.

---

### 구현

쿠폰 발급 요청을 받으면, 요청 시간을 score로 Redis Sorted Set에 userId를 저장합니다.

이때, 실제 DB 발급을 마치면 insert되는 Set에 해당 userId를 조회하여 중복 발급을 제외합니다.

이후 요청 API는 종료됩니다.

```java
    public void callIssueUserCoupon(UserCouponCommand.CallIssue command) {
        boolean isFail = !userCouponRepository.callIssue(command.user().getId(), command.couponId());
        if(isFail){
           throw new AlreadyIssuedException("이미 발급 요청한 쿠폰입니다.");
        }
    }
```

```java
public class RedisUserCouponRepository {
    public boolean callIssue (Long userId, Long couponId){
        long score = System.currentTimeMillis();
        if (isIssuedUser(userId, couponId)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(KEY_PREFIX + couponId + CALL_KEY_SUFFIX, "userId:" + userId, score));
    }

    private boolean isIssuedUser (Long userId, Long couponId){
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(KEY_PREFIX + couponId + ISSUED_KEY_SUFFIX, "userId:" + userId));
    }
}
```

10분마다 실행되는 Scheduler가 현재 발급중인 쿠폰의 발급 대기 유저를 조회해 쿠폰 발급을 진행합니다.

```java
    @Scheduled(cron = "0 */10 * * * *")
    public void hourlySalesProducts() {
        List<Coupon> issueCouponList = couponService.findIssueCouponList();
        issueCouponList.forEach(coupon -> couponFacade.issue(new CouponCriteria.Issue(coupon.getId())));
    }
```

발급 가능한 개수만큼만 Sorted Set에서 조회하여 유저들에게 쿠폰을 실제로 발급합니다.

```java
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issue(CouponCriteria.Issue criteria) {
        Coupon coupon = couponService.issueValidate(criteria.couponId());
        // 발급 상태가 종료상태이면 로직 종료
        if(coupon.getIssueStatus() == IssueStatus.FINISH) {
            return;
        }

        UserCouponCommand.FindIssueTarget command = new UserCouponCommand.FindIssueTarget(criteria.couponId(), coupon.getQuantity());
        List<Long> issueTargetUserIds = userCouponService.findIssueTargetUserIds(command);

        issueTargetUserIds.forEach(userId -> {
            User user = userService.findById(userId);
            UserCouponCommand.Issue issueCommand = new UserCouponCommand.Issue(user, coupon);
            userCouponService.issue(issueCommand);
            couponService.deduct(criteria.couponId());
        });
    }
```

조회에 성공하거나, 발급 완료 Set에는 없지만 실제로 쿠폰을 갖고 있는 고객들에 대해
발급 대기 Sorted Set에서 제거, 발급 완료 Set에 추가하여 이후 추가 발급 및 중복 발급을 방지합니다.
```java
    public void issueChecked(Long userId, Long couponId) {
        redisTemplate.opsForZSet().remove(KEY_PREFIX + couponId + CALL_KEY_SUFFIX, "userId:" + userId);
        redisTemplate.opsForSet().add(KEY_PREFIX + couponId + ISSUED_KEY_SUFFIX, "userId:" + userId);
    }
```

해당 기능을 구현하면서 가장 중요하게 본 내용은 통합 테스트였습니다.

실제로 Redis에 저장되고, 삭제되고, 추가되는 과정은 DB에 영속화 한 것 과는 달라, 데이터 클렌징과 실 데이터 확인을 위해 여러 테스트들을 구성하였습니다.

```java
        @DisplayName("쿠폰 발급 대상을 조회할 때 발급 수보다 많은 대기인원이 있어도 발급 수만큼만 조회한다.")
        @Test
        void findIssueTargetOverSetSize() {
            // given
            Coupon coupon = Coupon.create("4월 반짝 쿠폰", CouponType.TOTAL, DiscountType.FIXED, 5000, 3, LocalDate.now(), LocalDate.now().plusDays(3), 10);
            jpaCouponRepository.save(coupon);
            UserCouponCommand.FindIssueTarget command = new UserCouponCommand.FindIssueTarget(coupon.getId(), 5);

            final String KEY_PREFIX = "coupon:";
            final String CALL_KEY_SUFFIX = ":callIssue";
            String callKey = KEY_PREFIX + coupon.getId() + CALL_KEY_SUFFIX;
            long SystemTime = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(callKey, "userId:1", SystemTime);
            redisTemplate.opsForZSet().add(callKey, "userId:2", SystemTime + 1);
            redisTemplate.opsForZSet().add(callKey, "userId:3", SystemTime + 2);
            redisTemplate.opsForZSet().add(callKey, "userId:4", SystemTime + 3);
            redisTemplate.opsForZSet().add(callKey, "userId:5", SystemTime + 4);
            redisTemplate.opsForZSet().add(callKey, "userId:6", SystemTime + 5);
            redisTemplate.opsForZSet().add(callKey, "userId:7", SystemTime + 6);
            // when
            List<Long> issueTargetUserIds = userCouponService.findIssueTargetUserIds(command);

            // then
            assertThat(issueTargetUserIds).hasSize(5);
            assertThat(issueTargetUserIds).containsExactly(1L, 2L, 3L, 4L, 5L);
        }
```

이후에 Kafka를 활용하여 해당 기능을 재구현한다고 했는데, 얼마나 좋은 이점이 있을지 기대가 됩니다.