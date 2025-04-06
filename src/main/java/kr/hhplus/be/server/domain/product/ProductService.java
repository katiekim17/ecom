package kr.hhplus.be.server.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Product find(Long productId) {
        return productRepository.find(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하는 상품이 없습니다."));
    }
}
