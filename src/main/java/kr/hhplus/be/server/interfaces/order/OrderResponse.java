package kr.hhplus.be.server.interfaces.order;

public record OrderResponse(
        Long orderId,
        Long paymentId,
        int orderTotalAmount,
        int paymentTotalAmount
) {
}
