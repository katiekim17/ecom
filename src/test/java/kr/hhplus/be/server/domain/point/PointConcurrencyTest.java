package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infra.point.JpaPointRepository;
import kr.hhplus.be.server.infra.user.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaPointRepository jpaPointRepository;

    @DisplayName("동시에 여러번 충전을 진행하여도, 충전을 성공한 만큼만 포인트가 추가된다.")
    @Test
    void chargeConcurrency() throws InterruptedException{
        // given
        User user = jpaUserRepository.save(User.create("user"));
        jpaPointRepository.save(Point.create(user, 0));
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        int chargeAmount = 10;
        AtomicInteger successCnt = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    pointService.charge(new PointCommand.Charge(user, chargeAmount));
                    successCnt.getAndIncrement();
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 작업이 끝날 때까지 대기

        // then
        Point finalPoint = pointRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(finalPoint.getBalance()).isEqualTo(chargeAmount * successCnt.get()); // 총합 결과
    }

    @DisplayName("동시에 사용을 여러번 하여도 포인트가 충분한 경우 모두 사용되며, 포인트 차감이 정상 적용된다.")
    @Test
    void concurrencyUseAndCharge() throws InterruptedException {
        // given
        User user = jpaUserRepository.save(User.create("user"));
        jpaPointRepository.save(Point.create(user, 100));

        Long userId = user.getId();

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    pointService.use(new PointCommand.Use(user, 10));
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 작업이 끝날 때까지 대기

        // then
        Point finalPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(finalPoint.getBalance()).isEqualTo(0); // 총합 결과
    }
}
