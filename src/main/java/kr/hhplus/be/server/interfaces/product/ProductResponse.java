package kr.hhplus.be.server.interfaces.product;

public record ProductResponse(
        Long productId,
        String name,
        Integer stock,
        Integer price
) {
}
