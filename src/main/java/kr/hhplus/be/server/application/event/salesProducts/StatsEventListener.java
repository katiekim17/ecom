package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.stats.StatsCommand;
import kr.hhplus.be.server.domain.stats.StatsService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class StatsEventListener {

    private final StatsService statsService;

    public StatsEventListener(StatsService statsService) {
        this.statsService = statsService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCompleted(OrderCompletedEvent event) {
        OrderInfo orderInfo = event.orderInfo();
        StatsCommand.SaveSalesProductsByOrder command =
                new StatsCommand.SaveSalesProductsByOrder(
                        orderInfo.orderProducts(), orderInfo.orderDateTime());
        statsService.saveSalesProductByOrder(command);
    }
}