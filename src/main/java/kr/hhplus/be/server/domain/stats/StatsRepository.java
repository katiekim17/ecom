package kr.hhplus.be.server.domain.stats;

import kr.hhplus.be.server.infra.stats.SalesProductSummary;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository {
    List<PopularProduct> getPopularProducts();
    List<SalesProductSummary> findSalesProductSummaryByDateTime(LocalDateTime datetime);
    void batchInsert(List<SalesProductSummary> products);
}
