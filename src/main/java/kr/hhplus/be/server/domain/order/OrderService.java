package kr.hhplus.be.server.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order order(OrderCommand command) {
        Order order = Order.create(command.user(), command.discountInfo());

        command.orderLines().stream()
                    .map(orderProduct ->
                            OrderProduct.create(orderProduct.product(), orderProduct.quantity()))
                    .forEach(order::addOrderProduct);

        order.calculateTotalAmount();

        return orderRepository.save(order);
    }

    @Transactional
    public Order complete(Order order){
        order.complete();
        return order;
    }
}
