package kr.hhplus.be.server.application.ranking;

import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.ranking.RankingCommand;
import kr.hhplus.be.server.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;

    public void saveDailyRanking(RankingCriteria criteria) {

        RankingCommand.SaveDailyRanking rankingCommand = new RankingCommand.SaveDailyRanking(criteria.orderInfo().orderProducts(), criteria.orderInfo().orderDateTime());
        rankingService.saveDailyRanking(rankingCommand);
        // product cache 처리
        criteria.orderInfo().orderProducts().forEach(orderProduct -> {
            productService.find(orderProduct.getProductId());
        });
    }
}
