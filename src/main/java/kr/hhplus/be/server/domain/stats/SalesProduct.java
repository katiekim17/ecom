package kr.hhplus.be.server.domain.stats;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.product.Product;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "stats_daily_product_sales",
        indexes = {
                @Index(name = "idx_product_id_order_date", columnList = "product_id, order_date"),
        }
)
public class SalesProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    Long salesCount;

    LocalDate orderDate;
}
