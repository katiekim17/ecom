package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import kr.hhplus.be.server.domain.userCoupon.UserCouponInfo;

public record DiscountInfo(
        int discountAmount
) {
    public static DiscountInfo from(UserCoupon userCoupon) {
        return new DiscountInfo(userCoupon.getDiscountAmount());
    }

    public static DiscountInfo from(UserCouponInfo userCouponInfo) {
        return new DiscountInfo(userCouponInfo.discountAmount());
    }

    public static DiscountInfo empty() {
        return new DiscountInfo(0);
    }
}
