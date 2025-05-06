package kr.hhplus.be.server.infra.stats;

import java.time.LocalDate;

public interface SalesProductSummary {
    Long      getProductId();
    Long      getSalesCount();
    LocalDate getOrderDate();
}
