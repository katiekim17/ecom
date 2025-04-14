package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import org.springframework.stereotype.Repository;

@Repository
public class OrderReositoryImpl implements OrderRepository {
    @Override
    public Order save(Order order) {
        return null;
    }
}
