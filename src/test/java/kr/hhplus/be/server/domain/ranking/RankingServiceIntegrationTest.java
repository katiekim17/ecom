package kr.hhplus.be.server.domain.ranking;

import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductInfo;
import kr.hhplus.be.server.domain.stats.StatsCommand;
import kr.hhplus.be.server.infra.product.JpaProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Transactional
class RankingServiceIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private RankingService rankingService;

    @DisplayName("saveSalesProductByOrder를 호출하면 해당 OrderProduct를 Redis에 ZSet으로 저장한다.")
    @Test
    void saveSalesProductByOrder() {
        // given
        LocalDateTime orderDateTime = LocalDateTime.now();
        String key = "salesProduct:" + orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        // data cleansing
        redisTemplate.delete(key);
        Product product = Product.create("사과", 1000, 10);
        jpaProductRepository.save(product);
        List<OrderProduct> orderProducts = List.of(OrderProduct.create(ProductInfo.from(product), 2));
        StatsCommand.SaveSalesProductsByOrder command = new StatsCommand.SaveSalesProductsByOrder(orderProducts, orderDateTime);

        // when
        rankingService.saveDailyRanking(command);

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
        LocalDateTime orderDateTime = LocalDateTime.now();
        String key = "salesProduct:" + orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        // data cleansing
        redisTemplate.delete(key);
        Product product = Product.create("사과", 1000, 10);
        jpaProductRepository.save(product);
        List<OrderProduct> orderProducts = List.of(OrderProduct.create(ProductInfo.from(product), 2));
        StatsCommand.SaveSalesProductsByOrder command = new StatsCommand.SaveSalesProductsByOrder(orderProducts, orderDateTime);

        // when
        rankingService.saveDailyRanking(command);
        rankingService.saveDailyRanking(command);

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
        LocalDateTime firstOrderDateTime = LocalDateTime.now();
        LocalDateTime secondOrderDateTime = LocalDateTime.now().plusHours(1);
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
        rankingService.saveDailyRanking(firstCommand);
        rankingService.saveDailyRanking(secondCommand);

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