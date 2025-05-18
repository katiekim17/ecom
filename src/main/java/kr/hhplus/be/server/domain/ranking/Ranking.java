package kr.hhplus.be.server.domain.ranking;

import java.util.List;


public record Ranking(
        RankingType type,
        List<SalesProduct> products
) {

}