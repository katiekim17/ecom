package kr.hhplus.be.server.infra.stats;

import kr.hhplus.be.server.domain.stats.DailySalesProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStatsRepository extends JpaRepository<DailySalesProduct, Long> {
}
