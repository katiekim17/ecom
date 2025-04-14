package kr.hhplus.be.server.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<Product> find(Long id);
    void deleteAllInBatch();
    Page<Product> findAll(Pageable pageable);
    Product save(Product product);
    List<Product> saveAll(List<Product> products);

}
