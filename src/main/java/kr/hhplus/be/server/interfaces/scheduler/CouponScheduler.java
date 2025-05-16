package kr.hhplus.be.server.interfaces.scheduler;


import kr.hhplus.be.server.application.coupon.CouponCriteria;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponFacade couponFacade;
    private final CouponService couponService;

    @Scheduled(cron = "0 */10 * * * *")
    public void hourlySalesProducts() {
        List<Coupon> issueCouponList = couponService.findIssueCouponList();
        issueCouponList.forEach(coupon -> couponFacade.issue(new CouponCriteria.Issue(coupon.getId())));
    }

}
