package kr.hhplus.be.server.infra.userCoupon;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class RedisUserCouponRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "coupon:";
    private static final String CALL_KEY_SUFFIX = ":callIssue";
    private static final String ISSUED_KEY_SUFFIX = ":issued";

    public boolean callIssue(Long userId, Long couponId) {
        long score = System.currentTimeMillis();
        if(isIssuedUser(userId, couponId)){
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(KEY_PREFIX + couponId + CALL_KEY_SUFFIX, "userId:" + userId, score));
    }

    private boolean isIssuedUser(Long userId, Long couponId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(KEY_PREFIX + couponId + ISSUED_KEY_SUFFIX, "userId:" + userId));
    }

    public List<Long> findIssueTarget(Long couponId, int quantity){

        if(quantity <= 0){
            return List.of();
        }

        String key = KEY_PREFIX + couponId + CALL_KEY_SUFFIX;
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(key, 0, quantity - 1);

        if(typedTuples == null || typedTuples.isEmpty()){
            return List.of();
        }

        return typedTuples.stream().map(tuple -> {
            String userIdKey = String.valueOf(tuple.getValue());
            return Long.parseLong(userIdKey.split(":")[1]);
        }).toList();
    }

    public void issueChecked(Long userId, Long couponId) {
        redisTemplate.opsForZSet().remove(KEY_PREFIX + couponId + CALL_KEY_SUFFIX, "userId:" + userId);
        redisTemplate.opsForSet().add(KEY_PREFIX + couponId + ISSUED_KEY_SUFFIX, "userId:" + userId);
    }
}
