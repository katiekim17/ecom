package kr.hhplus.be.server.interfaces.product;

import kr.hhplus.be.server.domain.product.ProductInfo;

public record ProductResponse(
        Long productId,
        String name,
        Integer stock,
        Integer price
) {
    public static ProductResponse from(ProductInfo product) {
        return new ProductResponse(product.id(), product.name(), product.stock(), product.price());
    }
}
