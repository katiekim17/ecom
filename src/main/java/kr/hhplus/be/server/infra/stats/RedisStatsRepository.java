package kr.hhplus.be.server.infra.stats;

import kr.hhplus.be.server.domain.order.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisStatsRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveSalesProductsByOrder(List<OrderProduct> orderProducts, LocalDateTime orderDateTime) {
        // key는 yyyyMMddHH로 2025051401로 시간단위로 표시된다.
        String key = "salesProduct:" + orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

        for (OrderProduct orderProduct : orderProducts) {
            String value = "product:" + orderProduct.getProductId();
            redisTemplate.opsForZSet().incrementScore(key, value, orderProduct.getQuantity());
        }

        setExpire(key, orderDateTime);
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
