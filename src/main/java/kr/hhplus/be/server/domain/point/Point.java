package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Point {

    User user;
    int balance;

    @Builder
    private Point(User user, int balance) {
        this.user = user;
        this.balance = balance;
    }
}
