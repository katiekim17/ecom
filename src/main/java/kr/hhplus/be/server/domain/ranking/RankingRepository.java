package kr.hhplus.be.server.domain.ranking;

import kr.hhplus.be.server.domain.order.OrderProduct;

import java.time.LocalDateTime;
import java.util.List;

public interface RankingRepository {
    void saveDailyRanking(List<OrderProduct> orderProducts, LocalDateTime orderDateTime);
}
