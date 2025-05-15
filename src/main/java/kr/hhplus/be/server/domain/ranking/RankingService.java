package kr.hhplus.be.server.domain.ranking;

import kr.hhplus.be.server.domain.stats.StatsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional
    public void saveDailyRanking(StatsCommand.SaveSalesProductsByOrder command) {
        rankingRepository.saveDailyRanking(command.orderProducts(), command.orderDateTime());
    }

}
