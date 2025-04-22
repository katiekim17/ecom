package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.infra.userCoupon.JpaUserCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponFacadeConcurrencyTest {

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private JpaUserCouponRepository jpaUserCouponRepository;

    @DisplayName("쿠폰의 수량이 10개 남았을 때, 10명의 사용자가 발급을 동시에 요청하면 모두 발급되고 남은 수량은 0이 된다.")
    @Test
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
                    // 예외 로깅만 하고 넘어가기
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 작업이 끝날 때까지 대기

        // then
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        List<UserCoupon> issuedCoupons = jpaUserCouponRepository.findAll();
        assertThat(issuedCoupons).hasSize(10);
        assertThat(coupon.getQuantity()).isEqualTo(0);
    }

//    @DisplayName("쿠폰의 수량이 10개 남았을 때, 10명의 사용자가 발급을 동시에 요청하면 발급된 수 만큼만 쿠폰의 수량이 차감된다.")
//    @Test
//    @Sql(scripts = "/sql/userCoupon.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//    void issueConcurrency() throws InterruptedException{
//        // given
//        int threadCount = 10;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        Long couponId = 1L;
//
//        // when
//        for (long i = 1; i <= threadCount; i++) {
//            long userId = i;
//            executorService.submit(() -> {
//                try {
//                    CouponCriteria.IssueUserCoupon criteria = new CouponCriteria.IssueUserCoupon(userId, couponId);
//                    couponFacade.issueUserCoupon(criteria);
//                } catch (Exception e) {
//                    // 예외 로깅만 하고 넘어가기
//                    e.printStackTrace();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 작업이 끝날 때까지 대기
//
//        // then
//        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
//        List<UserCoupon> issuedCoupons = jpaUserCouponRepository.findAll();
//        assertThat(coupon.getQuantity()).isEqualTo(coupon.getInitialQuantity() - issuedCoupons.size());
//    }

}