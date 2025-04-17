package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductTest {

    @DisplayName("create로 생성할 시 orderProduct의 price는 파라미터 product의 price와 동일한 값을 가진다.")
    @Test
    void createOrderProduct() {
        // given
        Product product = Product.create( "사과", 50, 5000);

        // when
        OrderProduct orderProduct = OrderProduct.create(product, 1);

        // then
        assertThat(orderProduct.getPrice()).isEqualTo(product.getPrice());
        assertThat(orderProduct.getName()).isEqualTo(product.getName());
    }

}