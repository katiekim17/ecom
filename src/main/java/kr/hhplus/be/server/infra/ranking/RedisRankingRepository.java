package kr.hhplus.be.server.infra.ranking;

import kr.hhplus.be.server.domain.order.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class RedisRankingRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYY_MM_DD_HH = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final String SALES_KEY_PREFIX = "salesProduct:";
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String DAILY_KEY = "popularProducts:daily";

    public void saveDailyRanking(List<OrderProduct> orderProducts, LocalDateTime orderDateTime) {
        // key는 yyyyMMddHH로 2025051401로 시간단위로 표시된다.
        String key = SALES_KEY_PREFIX + orderDateTime.format(YYYY_MM_DD_HH);

        for (OrderProduct orderProduct : orderProducts) {
            String value = PRODUCT_KEY_PREFIX + orderProduct.getProductId();
            redisTemplate.opsForZSet().incrementScore(key, value, orderProduct.getQuantity());
        }

        setExpire(key, orderDateTime);
    }
    public void saveRankingByDaily(LocalDate targetDate) {
        String date = targetDate.format(YYYY_MM_DD);
        String firstKey = SALES_KEY_PREFIX + date + String.format("%02d", 1);
        List<String> otherKeys = IntStream.rangeClosed(2, 24)
                .mapToObj(i -> SALES_KEY_PREFIX + date + String.format("%02d", i))
                .toList();
        redisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, DAILY_KEY);

        setExpire(DAILY_KEY, LocalDateTime.now());
    }


    private void setExpire(String key, LocalDateTime orderDateTime) {
        // 들어온 시간대 + 25시간
        LocalDateTime expireAtLdt = orderDateTime.plusHours(25)
                .truncatedTo(ChronoUnit.HOURS);

        Instant expireInstant = expireAtLdt
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant();

        if(hasExpire(key)){
            redisTemplate.expireAt(key, expireInstant);
        }
    }

    private boolean hasExpire(String key){
        return redisTemplate.getExpire(key) < 0;
    }

}
