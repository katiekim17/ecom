package kr.hhplus.be.server.application.stats;

import kr.hhplus.be.server.application.ranking.RankingFacade;
import kr.hhplus.be.server.domain.ranking.Ranking;
import kr.hhplus.be.server.domain.ranking.SalesProduct;
import kr.hhplus.be.server.domain.stats.DailySalesProduct;
import kr.hhplus.be.server.domain.stats.StatsCommand;
import kr.hhplus.be.server.domain.stats.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsFacade {

    private final StatsService statsService;
    private final RankingFacade rankingFacade;

    public void saveDailySalesProductStats(StatsCriteria criteria) {

        Ranking dailyRanking = rankingFacade.findDailyRankingProducts();
        List<SalesProduct> products = dailyRanking.products();
        List<DailySalesProduct> dailySalesProducts = products.stream().map(product -> {
            return DailySalesProduct.create(product.getProductId(), product.getSales(), criteria.orderDate());
        }).toList();
        StatsCommand.NewSaveDailySalesProducts command = new StatsCommand.NewSaveDailySalesProducts(dailySalesProducts);
        statsService.saveDailyProducts(command);
    }

}
