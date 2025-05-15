package kr.hhplus.be.server.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional
    public void saveDailyRanking(RankingCommand.SaveDailyRanking command) {
        // 일간 랭킹 저장
        rankingRepository.saveDailyRanking(command.orderProducts(), command.orderDateTime());
    }

}
