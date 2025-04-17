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
    public Order order(OrderCommand.Create command) {
        Order order = Order.create(command.user());

        command.orderLines().stream()
                    .map(orderLine -> OrderProduct.create(orderLine.product(), orderLine.quantity()))
                    .forEach(order::addOrderProduct);

        order.applyCoupon(command.userCouponInfo());
        order.complete();

        return orderRepository.save(order);
    }
}
