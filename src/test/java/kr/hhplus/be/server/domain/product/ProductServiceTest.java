package kr.hhplus.be.server.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Nested
    class find {
        @DisplayName("productId로 해당하는 상품을 조회할 수 있다.")
        @Test
        void success() {
            // given
            Long productId = 1L;
            Product req = makeProduct(productId, "사과", 5000, 50);
            when(productRepository.find(productId)).thenReturn(Optional.of(req));

            // when
            Product product = productService.find(productId);

            // then
            assertThat(product.getId()).isEqualTo(productId);
            verify(productRepository, times(1)).find(productId);
        }

        @DisplayName("productId에 해당하는 상품이 없을 시 IllegalArgumentException이 발생한다.")
        @Test
        void fail() {
            // given
            Long productId = 1L;

            // when // then
            assertThatThrownBy(() -> productService.find(productId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하는 상품이 없습니다.");

            verify(productRepository, times(1)).find(productId);
        }
    }

    private static Product makeProduct(Long productId, String name, int price, int stock) {
        return Product.builder().id(productId).name(name).price(price).stock(stock).build();
    }


}