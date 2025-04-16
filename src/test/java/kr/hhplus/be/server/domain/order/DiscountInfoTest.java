package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.coupon.CouponType;
import kr.hhplus.be.server.domain.coupon.DiscountType;
import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.domain.userCoupon.UserCouponInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountInfoTest {

    @DisplayName("empty로 생성 시 discountAmount 값은 0이다.")
    @Test
    void empty() {
        // given
        DiscountInfo discountInfo = DiscountInfo.empty();
        // when // then
        assertThat(discountInfo.discountAmount()).isEqualTo(0);
    }

    @DisplayName("userCoupon을 받은 from으로 생성 시 discountAmount 값은 userCoupon의 discountAmount이다.")
    @Test
    void fromUserCoupon() {
        // given
        int discountAmount = 1000;
        UserCoupon userCoupon = UserCoupon.builder().userId(1L).couponId(1L).discountAmount(discountAmount).build();

        // when
        DiscountInfo discountInfo = DiscountInfo.from(userCoupon);

        // then
        assertThat(discountInfo.discountAmount()).isEqualTo(discountAmount);
    }

    @DisplayName("userCouponInfo로 discountInfo를 생성할 수 있다.")
    @Test
    void fromUserCouponInfo() {
        // given
        int discountAmount = 1000;
        UserCouponInfo userCoupon = new UserCouponInfo(1L, 1L, 1L, "깜짝 쿠폰", CouponType.TOTAL, DiscountType.FIXED, discountAmount, null, null);

        // when
        DiscountInfo discountInfo = DiscountInfo.from(userCoupon);

        // then
        assertThat(discountInfo.discountAmount()).isEqualTo(discountAmount);
    }

}