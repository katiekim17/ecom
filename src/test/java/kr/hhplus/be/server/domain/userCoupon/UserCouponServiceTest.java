package kr.hhplus.be.server.domain.userCoupon;

import kr.hhplus.be.server.domain.common.PageResult;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private UserCouponService userCouponService;

    @DisplayName("userId로 해당 유저가 보유한 쿠폰을 모두 조회할 수 있다.")
    @Test
    void findAllByUserId() {
        // given
        Long userId = 1L;
        User user = User.create(userId, "yeop");
        UserCouponCommand.FindAll command = new UserCouponCommand.FindAll(userId, 1, 10);
        List<UserCoupon> userCoupons = List.of(
                UserCoupon.builder().id(1L).userId(1L).couponId(1L).build()
                , UserCoupon.builder().id(2L).userId(1L).couponId(1L).build()
        );
        when(userService.findByUserId(userId)).thenReturn(user);
        when(userCouponRepository.findCountByUserId(userId)).thenReturn(2L);
        when(userCouponRepository.findAllByUserId(command)).thenReturn(userCoupons);

        // when
        PageResult<UserCoupon> result = userCouponService.findAllByUserId(command);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);

        verify(userService, times(1)).findByUserId(userId);
        verify(userCouponRepository, times(1)).findCountByUserId(userId);
        verify(userCouponRepository, times(1)).findAllByUserId(command);
    }
}