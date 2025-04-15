package kr.hhplus.be.server.domain.order;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);
    void deleteAllInBatch();
    Optional<Order> findById(Long id);
}
