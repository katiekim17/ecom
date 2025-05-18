package kr.hhplus.be.server.domain.stats;

import java.util.List;

public record StatsCommand() {
    public record NewSaveDailySalesProducts(
            List<DailySalesProduct> dailySalesProducts
    ) {
    }
}
