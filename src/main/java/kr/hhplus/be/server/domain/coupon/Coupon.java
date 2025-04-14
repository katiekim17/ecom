package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.common.BaseEntity;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.userCoupon.UserCoupon;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int discountAmount;
    private int expirationMonth;
    private int initialQuantity;
    private int quantity;

    public UserCoupon issueTo(User user) {
        deductQuantity();

        return UserCoupon.builder()
                .couponId(this.id)
                .userId(user.getId())
                .name(this.name)
                .discountAmount(this.discountAmount)
                .expiredAt(LocalDate.now().plusMonths(this.expirationMonth))
                .build();
    }

    private void deductQuantity() {
        if(quantity <= 0){
            throw new IllegalArgumentException("발급 가능한 수량을 초과하였습니다.");
        }
        quantity--;
    }

    public static Coupon create(String name, int discountAmount, int expirationMonth, int initialQuantity) {
        return new Coupon(name, discountAmount, expirationMonth, initialQuantity, initialQuantity);
    }

    private Coupon(String name, int discountAmount, int expirationMonth, int initialQuantity, int quantity) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.expirationMonth = expirationMonth;
        this.initialQuantity = initialQuantity;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
