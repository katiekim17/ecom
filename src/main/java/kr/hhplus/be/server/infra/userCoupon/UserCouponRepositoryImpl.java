package kr.hhplus.be.server.infra.userCoupon;

import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.domain.userCoupon.UserCouponCommand;
import kr.hhplus.be.server.domain.userCoupon.UserCouponRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserCouponRepositoryImpl implements UserCouponRepository {
    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return null;
    }

    @Override
    public long findCountByUserId(Long userId) {
        return 0;
    }

    @Override
    public List<UserCoupon> findAllByUserId(UserCouponCommand.FindAll command) {
        return List.of();
    }

    @Override
    public Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {
        return Optional.empty();
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return Optional.empty();
    }
}
