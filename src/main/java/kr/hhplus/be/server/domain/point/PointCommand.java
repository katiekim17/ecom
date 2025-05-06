package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;

public record PointCommand(
) {
    public record Charge(
            User user,
            int amount
    ){

    }
    public record Use(
            User user,
            int amount
    ){

    }
}
