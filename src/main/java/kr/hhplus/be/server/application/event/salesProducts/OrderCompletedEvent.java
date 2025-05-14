package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.domain.order.OrderInfo;

public record OrderCompletedEvent(
        OrderInfo orderInfo
) {
}
