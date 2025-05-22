package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderInfo<orderType>(
        Long id,
        Long userId,
        List<OrderProduct> orderProducts,
        OrderStatus status,
        Long userCouponId,
        int orderAmount,
        int finalAmount,
        LocalDateTime orderDateTime,
        String orderType // "ECOMMERCE" or "CONCERT"
) {
    public static OrderInfo from(Order order) {
        return new OrderInfo(
                order.getId(),
                order.getUserId(),
                order.getOrderProducts(),
                order.getStatus(),
                order.getUserCouponId(),
                order.getOrderAmount(),
                order.getFinalAmount(),
                order.getOrderDateTime(),
                order.getOrderType() // 여기도 추가되어야 함
        );
    }

    public boolean isConcertReservation() {
        return "CONCERT".equalsIgnoreCase(orderType);
    }
}
