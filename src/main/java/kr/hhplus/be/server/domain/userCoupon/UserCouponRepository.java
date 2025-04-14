package kr.hhplus.be.server.domain.userCoupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {
    UserCoupon save(UserCoupon userCoupon);
    List<UserCoupon> saveAll(List<UserCoupon> userCoupons);
    Page<UserCoupon> findAllByUserId(Long userId, Pageable pageable);
    void deleteAllInBatch();
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
    Optional<UserCoupon> findById(Long id);
}
