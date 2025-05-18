package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.IssueStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserService;
import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.domain.userCoupon.UserCouponCommand;
import kr.hhplus.be.server.domain.userCoupon.UserCouponService;
import kr.hhplus.be.server.support.config.redis.DistributedLock;
import kr.hhplus.be.server.support.config.redis.LockType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final UserService userService;

    @Transactional
    @DistributedLock(topic = "coupon", key = "#criteria.couponId", type = LockType.FAIR)
    public CouponResult.IssueUserCoupon issueUserCoupon(CouponCriteria.IssueUserCoupon criteria) {

        Coupon coupon = couponService.issueValidate(criteria.couponId());
        UserCoupon userCoupon = userCouponService.issue(criteria.toCommand(coupon));
        couponService.deduct(criteria.couponId());

        return CouponResult.IssueUserCoupon.from(userCoupon);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issue(CouponCriteria.Issue criteria) {
        Coupon coupon = couponService.issueValidate(criteria.couponId());
        // 발급 상태가 종료상태이면 로직 종료
        if(coupon.getIssueStatus() == IssueStatus.FINISH) {
            return;
        }

        UserCouponCommand.FindIssueTarget command = new UserCouponCommand.FindIssueTarget(criteria.couponId(), coupon.getQuantity());
        List<Long> issueTargetUserIds = userCouponService.findIssueTargetUserIds(command);

        issueTargetUserIds.forEach(userId -> {
            User user = userService.findById(userId);
            UserCouponCommand.Issue issueCommand = new UserCouponCommand.Issue(user, coupon);
            userCouponService.issue(issueCommand);
            couponService.deduct(criteria.couponId());
        });
    }
}
