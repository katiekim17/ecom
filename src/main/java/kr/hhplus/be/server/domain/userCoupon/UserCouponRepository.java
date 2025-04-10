package kr.hhplus.be.server.domain.userCoupon;

import java.util.List;

public interface UserCouponRepository {
    UserCoupon save(UserCoupon userCoupon);
    long findCountByUserId(Long userId);
    List<UserCoupon> findAllByUserId(UserCouponCommand.FindAll command);
}
