package kr.hhplus.be.server.domain.stats;

import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductInfo;
import kr.hhplus.be.server.infra.product.JpaProductRepository;
import kr.hhplus.be.server.infra.stats.JpaStatsRepository;
import kr.hhplus.be.server.infra.stats.RedisStatsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Transactional
class StatsServiceIntegrationTest {

    @Autowired
    private StatsService statsService;

    @Autowired
    private JpaStatsRepository jpaStatsRepository;

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisStatsRepository redisStatsRepository;

    @DisplayName("datetime을 기준으로 판매된 상품의 집계 데이터를 저장할 수 있다.")
    @Test
    @Sql(scripts = "/sql/saveSalesProduct.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void saveSalesProductsByDateTime() {
        // given
        LocalDateTime targetDateTime = LocalDateTime.of(2025, 4, 17, 0, 0, 0);
        StatsCommand.SaveSalesProducts command = new StatsCommand.SaveSalesProducts(targetDateTime);

        // when
        statsService.saveSalesProductByDateTime(command);

        // then
        LocalDate date = targetDateTime.toLocalDate();
        List<SalesProduct> all = jpaStatsRepository.findByOrderDateWithProduct(date);
        assertThat(all).hasSize(5);
        assertThat(all).extracting("product.id", "salesCount", "orderDate")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, date),
        tuple(2L, 2L, date),
                tuple(3L, 3L, date),
                tuple(4L, 4L, date),
                tuple(5L, 5L, date));
    }

    @DisplayName("3일간 가장 판매가 많았던 상품 5개를 조회할 수 있다.")
    @Test
    @Sql(scripts = "/sql/popularProduct.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void popularProducts() {
        // given // when
        Objects.requireNonNull(redisCacheManager.getCache("popularProducts")).clear();
        StatsCommand.PopularProducts command = new StatsCommand.PopularProducts(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
        PopularProducts popularProducts = statsService.getPopularProducts(command);

        // then
        assertThat(popularProducts.getProducts()).hasSize(5);
    }

    @DisplayName("3일간 가장 판매가 많았던 상품이 조회되지 않는 경우 빈 배열이 반환된다.")
    @Test
    void emptyPopularProducts() {
        // given // when
        Objects.requireNonNull(redisCacheManager.getCache("popularProducts")).clear();
        StatsCommand.PopularProducts command = new StatsCommand.PopularProducts(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
        PopularProducts popularProducts = statsService.getPopularProducts(command);

        // then
        assertThat(popularProducts.getProducts()).isEmpty();
    }

    @DisplayName("saveSalesProductByOrder를 호출하면 해당 OrderProduct를 Redis에 ZSet으로 저장한다.")
    @Test
    void saveSalesProductByOrder() {
        // given
        LocalDateTime orderDateTime = LocalDateTime.of(2025, 5, 14, 12, 0 ,0);
        String key = "salesProduct:" + orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        // data cleansing
        redisTemplate.delete(key);
        Product product = Product.create("사과", 1000, 10);
        jpaProductRepository.save(product);
        List<OrderProduct> orderProducts = List.of(OrderProduct.create(ProductInfo.from(product), 2));
        StatsCommand.SaveSalesProductsByOrder command = new StatsCommand.SaveSalesProductsByOrder(orderProducts, orderDateTime);

        // when
        statsService.saveSalesProductByOrder(command);

        // then
        assertThat(redisTemplate.hasKey(key)).isTrue();
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
        assertThat(typedTuples).hasSize(1);
        assertThat(typedTuples).extracting("score", "value")
                .containsExactly(tuple((double)orderProducts.get(0).getQuantity()
                        , "product:" + product.getId()));
    }

    @DisplayName("같은 시간대에 동일한 상품의 주문이 2번 발생한 경우 score 값이 증가한다.")
    @Test
    void saveSalesProductByOrderSameProduct() {
        // given
        LocalDateTime orderDateTime = LocalDateTime.of(2025, 5, 14, 12, 0 ,0);
        String key = "salesProduct:" + orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        // data cleansing
        redisTemplate.delete(key);
        Product product = Product.create("사과", 1000, 10);
        jpaProductRepository.save(product);
        List<OrderProduct> orderProducts = List.of(OrderProduct.create(ProductInfo.from(product), 2));
        StatsCommand.SaveSalesProductsByOrder command = new StatsCommand.SaveSalesProductsByOrder(orderProducts, orderDateTime);

        // when
        statsService.saveSalesProductByOrder(command);
        statsService.saveSalesProductByOrder(command);

        // then
        assertThat(redisTemplate.hasKey(key)).isTrue();
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
        assertThat(typedTuples).hasSize(1);
        assertThat(typedTuples).extracting("score", "value")
                .containsExactly(tuple(4.0, "product:" + product.getId()));
    }

    @DisplayName("다른 시간대에 동일한 상품의 주문이 2번 발생한 경우 별도의 key로 각각 저장된다.")
    @Test
    void saveSalesProductByOrderDifferentTime() {
        // given
        LocalDateTime firstOrderDateTime = LocalDateTime.of(2025, 5, 14, 12, 0 ,0);
        LocalDateTime secondOrderDateTime = LocalDateTime.of(2025, 5, 14, 13, 0 ,0);
        String firstKey = "salesProduct:" + firstOrderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String secondKey = "salesProduct:" + secondOrderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        // data cleansing
        redisTemplate.delete(List.of(firstKey, secondKey));
        Product product = Product.create("사과", 1000, 10);
        jpaProductRepository.save(product);
        List<OrderProduct> orderProducts = List.of(OrderProduct.create(ProductInfo.from(product), 2));
        StatsCommand.SaveSalesProductsByOrder firstCommand = new StatsCommand.SaveSalesProductsByOrder(orderProducts, firstOrderDateTime);
        StatsCommand.SaveSalesProductsByOrder secondCommand = new StatsCommand.SaveSalesProductsByOrder(orderProducts, secondOrderDateTime);

        // when
        statsService.saveSalesProductByOrder(firstCommand);
        statsService.saveSalesProductByOrder(secondCommand);

        // then
        assertThat(redisTemplate.hasKey(firstKey)).isTrue();
        assertThat(redisTemplate.hasKey(secondKey)).isTrue();
        Set<ZSetOperations.TypedTuple<Object>> firstTuples = redisTemplate.opsForZSet().rangeWithScores(firstKey, 0, -1);
        assertThat(firstTuples).hasSize(1);
        assertThat(firstTuples).extracting("score", "value")
                .containsExactly(tuple(2.0, "product:" + product.getId()));
        Set<ZSetOperations.TypedTuple<Object>> secondTuples = redisTemplate.opsForZSet().rangeWithScores(secondKey, 0, -1);
        assertThat(secondTuples).hasSize(1);
        assertThat(secondTuples).extracting("score", "value")
                .containsExactly(tuple(2.0, "product:" + product.getId()));
    }

}