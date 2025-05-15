package kr.hhplus.be.server.infra.ranking;

import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RedisRankingRepository redisRankingRepository;

    @Override
    public void saveDailyRanking(List<OrderProduct> orderProducts, LocalDateTime orderDateTime) {
        redisRankingRepository.saveDailyRanking(orderProducts, orderDateTime);
    }
}
