package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.infra.coupon.JpaCouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponServiceIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CouponService couponService;

    @Autowired
    private JpaCouponRepository jpaCouponRepository;

    @AfterEach
    void tearDown() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }

    @DisplayName("쿠폰을 저장할 수 있다.")
    @Test
    void test() {
        // given
        CouponCommand.Register command = new CouponCommand.Register("5000원 반짝 쿠폰", CouponType.TOTAL, DiscountType.FIXED, 5000, 3, LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), 10);

        // when
        Coupon coupon = couponService.register(command);

        // then
        Coupon findCoupon = jpaCouponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(findCoupon).isNotNull();
    }

}