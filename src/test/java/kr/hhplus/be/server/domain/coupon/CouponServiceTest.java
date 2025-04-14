package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserService;
import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.domain.userCoupon.UserCouponRepository;
import kr.hhplus.be.server.support.exception.AlreadyIssuedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Nested
    class issue{
        @DisplayName("쿠폰 발급을 할 수 있다.")
        @Test
        void success() {
            // given
            Long userId = 1L;
            Long couponId = 1L;
            CouponCommand command = new CouponCommand(userId, couponId);
            User user = User.create("yeop");
            Coupon coupon = Coupon.create(couponId, "4월 깜짝 쿠폰", 5000, 5, 50);
            UserCoupon userCoupon = UserCoupon.builder().couponId(couponId).userId(userId).build();

            when(userService.findById(userId)).thenReturn(user);
            when(couponRepository.findById(couponId)).thenReturn(coupon);
            when(userCouponRepository.save(userCoupon)).thenReturn(userCoupon);

            // when
            UserCoupon issuedCoupon = couponService.issue(command);

            // then
            assertThat(issuedCoupon).isEqualTo(userCoupon);
            verify(userService, times(1)).findById(userId);
            verify(couponRepository, times(1)).findById(couponId);
            verify(userCouponRepository, times(1)).save(userCoupon);
        }

        @DisplayName("동일한 쿠폰을 발급받은 유저가 다시 발급하면, AlreadyIssuedException이 발생한다.")
        @Test
        void fail() {
            // given
            Long userId = 1L;
            Long couponId = 1L;
            Long userCouponId = 1L;
            CouponCommand command = new CouponCommand(userId, couponId);
            User user = User.create("yeop");
            Coupon coupon = Coupon.create(couponId, "4월 깜짝 쿠폰", 5000, 5, 50);
            UserCoupon userCoupon = UserCoupon.builder().id(userCouponId).couponId(couponId).userId(userId).build();

            when(userService.findById(userId)).thenReturn(user);
            when(couponRepository.findById(couponId)).thenReturn(coupon);
            when(userCouponRepository.findByUserIdAndCouponId(userId, couponId)).thenReturn(Optional.of(userCoupon));

            // when
            assertThatThrownBy(() -> couponService.issue(command))
                    .isInstanceOf(AlreadyIssuedException.class)
                    .hasMessage("이미 발급 받은 쿠폰입니다.");

            // then
            verify(userService, times(1)).findById(userId);
            verify(couponRepository, times(1)).findById(couponId);
            verify(userCouponRepository, times(1)).findByUserIdAndCouponId(userId, couponId);
        }
    }

}