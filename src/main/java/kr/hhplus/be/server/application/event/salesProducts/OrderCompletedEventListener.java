package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.application.event.salesProducts.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.OrderInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCompletedEventListener {

    private final ApplicationEventPublisher publisher;

    @Async
    @EventListener
    public void handle(OrderCompletedEvent event) {
        OrderInfo orderInfo = event.orderInfo();

        publisher.publishEvent(new OrderToExternalSystemEvent(orderInfo));
    }
}
