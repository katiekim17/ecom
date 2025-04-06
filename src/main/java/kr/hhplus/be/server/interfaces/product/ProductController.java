package kr.hhplus.be.server.interfaces.product;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductDocs{

    private final ProductService productService;

    @GetMapping("/api/v1/products/{productId}")
    public ResponseEntity<ProductResponse> product(@PathVariable Long productId){
        Product product = productService.find(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping("/api/v1/products")
    public ResponseEntity<List<ProductResponse>> products(){
        return ResponseEntity.ok(List.of(new ProductResponse(1L, "맥북", 50, 100000)));
    }

}
