package kr.hhplus.be.server.domain.product;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Product {
    private Long id;
    private String name;
    private int stock;
    private int price;

    @Builder
    private Product(Long id, String name, int stock, int price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }
}
