package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infra.order.JpaOrderRepository;
import kr.hhplus.be.server.infra.payment.JpaPaymentRepository;
import kr.hhplus.be.server.infra.point.JpaPointRepository;
import kr.hhplus.be.server.infra.product.JpaProductRepository;
import kr.hhplus.be.server.infra.user.JpaUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderFacadeTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaPointRepository jpaPointRepository;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @AfterEach
    void tearDown() {
        jpaPaymentRepository.deleteAllInBatch();
        jpaOrderRepository.deleteAllInBatch();
        jpaPointRepository.deleteAllInBatch();
        jpaUserRepository.deleteAllInBatch();
        jpaProductRepository.deleteAllInBatch();
    }

    @DisplayName("동시에 동일한 상품으로 주문을 해도 정상적으로 재고가 차감된다.")
    @Test
    void concurrencyOrderByProductDeduct() throws InterruptedException {
        // given
        Product product1 = productRepository.save(Product.create("사과", 10, 10));
        Product product2 = productRepository.save(Product.create("배", 10, 10));


        int threadCount = 12;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            User user = jpaUserRepository.save(User.create("user" + i));
            jpaPointRepository.save(Point.create(user, 1000));
        }

        // when
        for (long i = 1; i <= threadCount; i++) {
            Long userId = i;
            executorService.submit(() -> {
                try{
                    User user = jpaUserRepository.findById(userId).orElseThrow();
                    OrderCriteria.Create criteria = new OrderCriteria.Create(user, null, List.of(
                            new OrderCriteria.Create.OrderItem(product1.getId(), 1),
                            new OrderCriteria.Create.OrderItem(product2.getId(), 1))
                    );
                    orderFacade.order(criteria);
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
        assertThat(successCnt.get()).isNotZero();
        List<Product> products = jpaProductRepository.findAll();
        int leftStock = product1.getStock() - successCnt.get();
        assertThat(products).extracting(Product::getStock)
                .containsOnly(leftStock, leftStock);
    }

    @DisplayName("")
    @Test
    void concurrencyOrderByPoint() {
        // given


        // when


        // then
    }
}