package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.user.User;

public record PaymentCommand(
) {
    public record Pay(
            Order order,
            User user
    ) {

    }
}
