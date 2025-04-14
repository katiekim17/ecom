package kr.hhplus.be.server.domain.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.support.exception.InsufficientBalanceException;
import kr.hhplus.be.server.support.exception.MaximumBalanceException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    private int balance;

    public static Point create(User user, int balance) {
        return new Point(user, balance);
    }

    private Point(User user, int balance) {
        this.user = user;
        this.balance = balance;
    }

    public void charge(int amount) {
        int balance = this.balance + amount;

        if(PointRule.CHARGE.getMin() >= amount){
            throw new IllegalArgumentException("포인트는 1포인트 이상부터 충전이 가능합니다.");
        }else if(PointRule.CHARGE.getMax() < balance){
            throw new MaximumBalanceException();
        }

        this.balance = balance;
    }

    public void use(int amount) {

        int usedBalance = this.balance - amount;

        if(amount <= 0){
            throw new IllegalArgumentException("포인트는 1포인트 이상부터 사용 가능합니다.");
        }else if(usedBalance < 0){
            throw new InsufficientBalanceException();
        }
        balance = usedBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;
        return balance == point.balance && Objects.equals(user, point.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, balance);
    }
}
