package kr.hhplus.be.server.infra;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CouponRepositoryImpl implements CouponRepository {
    @Override
    public Coupon findById(Long aLong) {
        return null;
    }

    @Override
    public Coupon save(Coupon coupon) {
        return null;
    }
}
