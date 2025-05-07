package kr.hhplus.be.server.support.config.redis;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)   // 트랜잭션 AOP 전에 실행
public class DistributedLockAspect {

    private final RedissonClient redisson;

    @Around("@annotation(kr.hhplus.be.server.support.config.redis.DistributedLock)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        DistributedLock ann = method.getAnnotation(DistributedLock.class);

        String lockKey = parseKey(ann.key(), sig.getParameterNames(), pjp.getArgs());

        RLock lock = ann.type() == LockType.FAIR
                ? redisson.getFairLock(lockKey) : redisson.getLock(lockKey);

        boolean acquired = lock.tryLock(
                ann.waitTime(), ann.leaseTime(), ann.unit());
        if (!acquired) {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }
        // 트랜잭션 동기화가 활성화되어 있으면 커밋/롤백 후 해제하도록 등록
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        }
                    });
            // 실제 메서드 실행
            return pjp.proceed();
        }
        // 트랜잭션 없으면, 메서드 종료 후 바로 해제
        try {
            return pjp.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String parseKey(String spel, String[] paramNames, Object[] args) {
        // 1) SpEL 파서 & 컨텍스트
        StandardEvaluationContext ctx = new StandardEvaluationContext();

        // 2) 파라미터 이름과 값을 컨텍스트에 바인딩
        for (int i = 0; i < paramNames.length; i++) {
            ctx.setVariable(paramNames[i], args[i]);
        }

        // 3) SpEL 평가 후 키 반환
        return new SpelExpressionParser()
                .parseExpression(spel)
                .getValue(ctx, String.class);
    }
}
