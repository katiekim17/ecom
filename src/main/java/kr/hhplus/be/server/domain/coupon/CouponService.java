package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserService;
import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.domain.userCoupon.UserCouponRepository;
import kr.hhplus.be.server.support.exception.AlreadyIssuedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final UserService userService;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public UserCoupon issue(CouponCommand command) {
        User user = userService.findByUserId(command.userId());
        Coupon coupon = couponRepository.findById(command.couponId());

        userCouponRepository
                .findByUserIdAndCouponId(command.userId(), command.couponId())
                .ifPresent(a -> {throw new AlreadyIssuedException("이미 발급 받은 쿠폰입니다.");});

        UserCoupon userCoupon = coupon.issueTo(user);

        couponRepository.save(coupon);

        return userCouponRepository.save(userCoupon);
    }

}
