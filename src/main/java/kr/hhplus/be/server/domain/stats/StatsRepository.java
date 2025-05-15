package kr.hhplus.be.server.domain.stats;

import java.util.List;

public interface StatsRepository {
    void saveAll(List<DailySalesProduct> list);
}
