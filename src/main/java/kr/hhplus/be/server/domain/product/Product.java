package kr.hhplus.be.server.domain.product;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
public class Product {
    private Long id;
    private String name;
    private int stock;
    private int price;

    private Product(Long id, String name, int stock, int price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static Product create(Long id, String name, int stock, int price){
        return new Product(id, name, stock, price);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;
        return stock == product.stock && price == product.price && Objects.equals(id, product.id) && Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, stock, price);
    }
}
