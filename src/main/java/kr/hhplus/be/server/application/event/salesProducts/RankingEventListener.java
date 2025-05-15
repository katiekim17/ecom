package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.ranking.RankingService;
import kr.hhplus.be.server.domain.stats.StatsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RankingEventListener {

    private final RankingService rankingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCompleted(OrderCompletedEvent event) {
        OrderInfo orderInfo = event.orderInfo();
        StatsCommand.SaveSalesProductsByOrder command =
                new StatsCommand.SaveSalesProductsByOrder(
                        orderInfo.orderProducts(), orderInfo.orderDateTime());
        rankingService.saveDailyRanking(command);
    }
}