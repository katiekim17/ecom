package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Point {

    private User user;
    private int balance;

    @Builder
    private Point(User user, int balance) {
        this.user = user;
        this.balance = balance;
    }

    public void charge(int amount) {
        int balance = this.balance + amount;

        if(PointRule.CHARGE.getMin() >= amount){
            throw new IllegalArgumentException("포인트는 1포인트 이상부터 충전이 가능합니다.");
        }else if(PointRule.CHARGE.getMax() < balance){
            throw new IllegalArgumentException("충전 이후 포인트는 10,000,000포인트를 넘을 수 없습니다.");
        }

        this.balance = balance;
    }
}
