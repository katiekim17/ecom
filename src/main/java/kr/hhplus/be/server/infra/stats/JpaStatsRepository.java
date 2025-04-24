package kr.hhplus.be.server.infra.stats;

import kr.hhplus.be.server.domain.stats.SalesProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface JpaStatsRepository extends JpaRepository<SalesProduct, Long> {

    @Query(value = """
        SELECT op.product_id AS productId, 
               SUM(op.quantity) AS totalQuantity, 
               p.name, 
               p.price, 
               p.stock
        FROM order_product op
        JOIN orders o ON op.order_id = o.id
        JOIN product p ON op.product_id = p.id
        WHERE o.status = 'SUCCESS'
          AND o.order_date_time >= NOW() - INTERVAL 3 DAY
        GROUP BY op.product_id, p.name, p.price, p.stock
        ORDER BY totalQuantity DESC
        LIMIT 5
    """, nativeQuery = true)
    List<NativePopularProduct> getPopularProducts();

    @Query(value = """
    SELECT
      op.product_id          AS productId,
      SUM(op.quantity)       AS salesCount,
      CURDATE()              AS orderDate
    FROM orders o
    JOIN order_product op ON op.order_id = o.id
    WHERE o.order_date_time >= :dateTime
      AND o.status = 'SUCCESS'
    GROUP BY op.product_id
    ORDER BY salesCount DESC
    """, nativeQuery = true)
    List<SalesProductSummary> findSalesProductByDateTime(@Param("dateTime")LocalDateTime dateTime);

    @Query("""
      SELECT sp
        FROM SalesProduct sp
        JOIN FETCH sp.product p
       WHERE sp.orderDate = :orderDate
    """)
    List<SalesProduct> findByOrderDateWithProduct(@Param("orderDate") LocalDate orderDate);
}
