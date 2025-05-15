package kr.hhplus.be.server.domain.ranking;

import kr.hhplus.be.server.domain.order.OrderProduct;

import java.time.LocalDateTime;
import java.util.List;

public record RankingCommand() {
    public record SaveDailyRanking(
            List<OrderProduct> orderProducts,
            LocalDateTime orderDateTime
    ) {

    }
}
