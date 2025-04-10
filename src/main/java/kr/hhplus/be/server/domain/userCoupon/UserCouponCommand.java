package kr.hhplus.be.server.domain.userCoupon;

public record UserCouponCommand(

) {
    public record FindAll(
            Long userId, int pageNo, int pageSize
    ){

    }
}
