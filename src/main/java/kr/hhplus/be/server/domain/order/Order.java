package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.BaseEntity;
import kr.hhplus.be.server.domain.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table(name = "orders")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private final List<OrderProduct> orderProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Transient
    private DiscountInfo discountInfo;

    private int orderAmount;

    private int finalAmount;

    private LocalDateTime orderDateTime;

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
    }

    public void calculateTotalAmount() {
        orderProducts.forEach(orderProduct ->
                orderAmount += orderProduct.getPrice() * orderProduct.getQuantity());
        int discountAmount = orderAmount - discountInfo.discountAmount();

        finalAmount = Math.max(discountAmount, 0);
    }

    public void complete(){
        this.status = OrderStatus.SUCCESS;
    }

    public static Order create(User user, DiscountInfo discountInfo){
        return new Order(user, OrderStatus.PENDING, discountInfo, LocalDateTime.now());
    }

    private Order(User user, OrderStatus status, DiscountInfo discountInfo, LocalDateTime orderDateTime) {
        this.user = user;
        this.status = status;
        this.discountInfo = discountInfo;
        this.orderDateTime = orderDateTime;
    }
}
