package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.domain.order.OrderInfo;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

public class ExternalOrderSyncListener {
    @Async
    @EventListener
    public void handle(OrderToExternalSystemEvent event) {
        OrderInfo orderInfo = event.orderInfo();

        if (orderInfo.isConcertReservation()) {
            System.out.println("[MOCK] 콘서트 예약 외부 전송: " + orderInfo);
        } else {
            System.out.println("[MOCK] 일반 주문 외부 전송: " + orderInfo);
        }
    }
}
