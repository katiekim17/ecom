package kr.hhplus.be.server.domain.stats;

import kr.hhplus.be.server.domain.order.OrderProduct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StatsCommand() {

    public record PopularProducts(
            LocalDate startDate,
            LocalDate endDate
    ) {

    }

    public record SaveSalesProducts (
            LocalDateTime dateTime
    ) {
        public SaveSalesProducts {
            if (dateTime == null) {
                throw new IllegalArgumentException("날짜 값이 입력되지 않았습니다.");
            }
        }
    }

    public record SaveSalesProductsByOrder (
            List<OrderProduct> orderProducts,
            LocalDateTime orderDateTime
    ) {

    }
}
