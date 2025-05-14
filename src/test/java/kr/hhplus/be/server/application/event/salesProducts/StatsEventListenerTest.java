package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductInfo;
import kr.hhplus.be.server.domain.stats.StatsCommand;
import kr.hhplus.be.server.domain.stats.StatsService;
import kr.hhplus.be.server.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatsEventListenerTest {

    @Mock
    StatsService statsService;

    @InjectMocks
    StatsEventListener listener;

    @DisplayName("주문 완료 event가 발행되면 listener는 statsService의 saveSalesProductByOrder를 호출한다.")
    @Test
    void orderCompletedEvent() {
        // given
        OrderProduct orderProduct = OrderProduct.create(ProductInfo.from(Product.create("사과", 1000, 10)), 2);
        Order order = Order.create(User.create("yeop"));
        order.addOrderProduct(orderProduct);
        OrderInfo orderInfo = OrderInfo.from(order);
        StatsCommand.SaveSalesProductsByOrder command = new StatsCommand.SaveSalesProductsByOrder(orderInfo.orderProducts(), orderInfo.orderDateTime());
        OrderCompletedEvent event = new OrderCompletedEvent(orderInfo);

        doNothing().when(statsService).saveSalesProductByOrder(command);
        // when
        listener.handleOrderCompleted(event);

        // then
        verify(statsService).saveSalesProductByOrder(command);
    }
    

}