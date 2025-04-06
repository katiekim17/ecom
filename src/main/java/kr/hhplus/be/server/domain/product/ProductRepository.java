package kr.hhplus.be.server.domain.product;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository {

    public Optional<Product> find(Long productId);

}
