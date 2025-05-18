# 레디스를 활용한 실시간 판매 순위 보고서

## 개요

현재 API에 구현되어 있는 유일한 통계 데이터는 3일간 가장 많은 판매가 이루어진 상품을 조회하는 API입니다.

```java
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "popularProducts",
            key        = "#command.startDate().toString() + '_' + #command.endDate().toString()"
    )
    public List<PopularProduct> getPopularProducts(StatsCommand.PopularProducts command) {
        return statsRepository.getPopularProducts(command.startDate(), command.endDate());
    }
```

해당 로직은 캐싱 처리가 되어 있지만, TTL이 25시간으로, scheduler가 하루에 한 번 해당 데이터를 최신화합니다.

위 데이터를 아래와 같은 데이터로 변경하고자 합니다.

1. 인기 상품 데이터는 1시간마다 직전 24시간 이내의 판매 상품을 조회할 수 있도록 변경한다.
2. 주문이 성공하면 해당 주문에 요청된 상품 데이터가 적재되며, 데이터 적재는 주문에게 영향을 주지 않도록 구성한다.
3. 해당 데이터는 Redis를 통해 캐싱처리를 하며, 별도 상품 조회에도 사용할 수 있게 인기 상품의 대한 상품 정보는 별도의 캐시로 구성한다.
4. 주문이 성공하면 해당 주문에 요청된 상품 데이터가 적재되며, 데이터 적재는 주문에게 영향을 주지 않도록 구성한다.
5. 해당 인기 상품 데이터를 추후 일간, 주간, 월간 데이터에 활용될 수 있도록 DB에 영속화한다.

이를 구현하기 위해 주문 완료 시 OrderCompletedEvent를 발행하여, RankingEventListener가 해당 이벤트를 수신해 데이터를 적재합니다.

```java
    @Transactional
    @DistributedLock(topic = "product", key = "#criteria.toLockKeys()")
    public OrderResult order(OrderCriteria.Create criteria) {
    
        // ...

        publisher.publishEvent(new OrderCompletedEvent(OrderInfo.from(order)));

        return new OrderResult(
                order.getId(), payment.getId(),
                order.getOrderAmount(), payment.getTotalAmount());
    }
```
발행된 Event는 아래의 Listener가 수신하며, RankingFacade를 통해 해당 데이터를 Redis에 적재합니다. 

```java
@Component
@RequiredArgsConstructor
public class RankingEventListener {

    private final RankingFacade rankingFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCompleted(OrderCompletedEvent event) {
        RankingCriteria criteria = new RankingCriteria(event.orderInfo());
        rankingFacade.saveSalesProduct(criteria);
    }
}
```

RankingFacade는 우선, 랭킹 정보를 담은 Redis Sorted Set에 저장하며, 이후 productService의 find를 호출하여 해당 상품들을 캐싱합니다.

```java
    public void saveSalesProduct(RankingCriteria criteria) {

        RankingCommand.SaveSalesProduct rankingCommand = new RankingCommand.SaveSalesProduct(criteria.orderInfo().orderProducts(), criteria.orderInfo().orderDateTime());
        rankingService.saveSalesProduct(rankingCommand);
        // product cache 처리
        criteria.orderInfo().orderProducts().forEach(orderProduct -> {
            productService.find(orderProduct.getProductId());
        });
    }
```

```java
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "product", key = "#id")
    public ProductInfo find(Long id) {
        Product product = productRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하는 상품이 없습니다."));
        return ProductInfo.from(product);
    }
```

판매 상품들은 RedisRepository가 현재 일자 + 시간대로 key를 만들어 랭킹 정보를 집계합니다.

```java
    public void saveSalesProduct(List<OrderProduct> orderProducts, LocalDateTime orderDateTime) {
        // key는 yyyyMMddHH로 2025051401로 시간단위로 표시된다.
        String key = SALES_KEY_PREFIX + orderDateTime.format(YYYY_MM_DD_HH);

        for (OrderProduct orderProduct : orderProducts) {
            String value = PRODUCT_KEY_PREFIX + orderProduct.getProductId();
            redisTemplate.opsForZSet().incrementScore(key, value, orderProduct.getQuantity());
        }

        setSalesProductExpire(key, orderDateTime);
    }
```


만들어진 집계 데이터를 기반하여 스케줄러가 일간 랭킹을 생성하도록 서비스를 호출해주며,

```java
    @Scheduled(cron = "0 0 0-23 * * *")
    public void hourlySalesProducts() {
        LocalDateTime now = LocalDateTime.now();
        RankingCommand.SaveDailyRanking command = new RankingCommand.SaveDailyRanking(now);
        rankingService.saveDailyRanking(command);
    }
```

호출된 서비스는 1시간 마다 24시간 window를 이동시켜 실시간 판매 랭킹을 업데이트합니다. 

```java
    public void saveDailyRanking(LocalDateTime targetDateTime) {

        LocalDateTime startDateTime = targetDateTime.minusHours(24);
        String firstKey = SALES_KEY_PREFIX + startDateTime.format(YYYY_MM_DD_HH);

        List<String> otherKeys = new ArrayList<>();
        for( int i = 1; i < 24; i++){
            otherKeys.add(SALES_KEY_PREFIX + startDateTime.plusHours(i).format(YYYY_MM_DD_HH));
        }

        redisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, DAILY_KEY);
        redisTemplate.expire(DAILY_KEY, 70, TimeUnit.MINUTES);
    }
```

그렇게 만들어진 일간 랭킹 데이터와 캐싱된 상품 데이터를 조합하여 일간 판매 상품 순위 데이터를 조회합니다.

```java
    @Transactional(readOnly = true)
    public Ranking findDailyRankingProducts() {
        List<SalesProduct> dailySalesProducts = rankingService.findDailySalesProducts();
        dailySalesProducts.forEach(salesProduct -> {
            ProductInfo productInfo = productService.find(salesProduct.getProductId());
            salesProduct.setProductInfo(productInfo);
        });
        return new Ranking(RankingType.DAILY, dailySalesProducts);
    }
```

```java
    public List<SalesProduct> findDailySalesProducts() {

        Set<ZSetOperations.TypedTuple<Object>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(DAILY_KEY, 0, -1);

        if(typedTuples == null || typedTuples.isEmpty()){
            return List.of();
        }

        return typedTuples.stream().map(tuple -> {
            String key = String.valueOf(tuple.getValue());
            Long productId = Long.parseLong(key.split(PRODUCT_KEY_PREFIX)[1]);
            int sales = Objects.requireNonNull(tuple.getScore()).intValue();
            return SalesProduct.create(productId, sales);
        }).toList();
    }
```

매일 00:00에는 직전 24시간 즉, 00:00 ~ 23:59의 상품 데이터가 저장되므로, 해당 시간에 스케줄러가 DB에 영속화를 진행합니다.

```java
@Component
@RequiredArgsConstructor
public class StatsScheduler {

    private final StatsFacade statsFacade;

    @Scheduled(cron = "0 0 0 * * *")
    public void saveDailyProductStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        StatsCriteria criteria = new StatsCriteria(yesterday);
        statsFacade.saveDailySalesProductStats(criteria);
    }

}
```


회고 

캐싱을 통한 집계 처리에 대해 경험해볼 수 있어서 매우 좋았습니다.

설계 단계에서 많은 어려움을 겪었는데, 실제로 작업을 진행하는 와중에도 여러번 설계가 수정되었습니다.

Ranking(실제 랭킹 데이터)과 Stats(DB에 적재되는 통계 데이터)의 역할을 분리하여 최대한 관심사를 나눠 가지려고 노력하는 과정이 조금 재미있었고, 그 부분에서 많이 배웠던 것 같습니다.

실제로는 일간, 주간, 월간 데이터도 진행하고자 하였는데, 생각보다 설계 부분에서 너무 시간을 많이 잡아먹어서 따로 구현해보진 못했습니다.

추후에 남는 시간에 열심히 구현해보도록 하겠습니다.

중간 중간 메서드 명 때문에 고민이 많이 되어서 우선 아무렇게나 지어놓고 추후에 생각나면 고쳤는데, 잘 맞는지는 모르겠습니다.