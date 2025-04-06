package kr.hhplus.be.server.interfaces.order;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrderDocs{

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderResponse> order(@Valid @RequestBody OrderRequest.Create request) {
        return ResponseEntity.ok(new OrderResponse(1L, 1L, 10000, 10000));
    }

}
