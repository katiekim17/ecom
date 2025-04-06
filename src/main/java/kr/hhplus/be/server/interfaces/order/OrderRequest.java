package kr.hhplus.be.server.interfaces.order;

import java.util.List;

public record OrderRequest(
) {

    public record Create(
            Long userId,
            Long userCouponId,
            List<OrderItem> orderItems
    ){

    }

    public record OrderItem(
            Long productId,
            int quantity
    ) {
    }
}
