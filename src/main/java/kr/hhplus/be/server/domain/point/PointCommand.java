package kr.hhplus.be.server.domain.point;

public record PointCommand(
) {
    public record CHARGE(
            Long userId,
            int amount
    ){

    }
    public record USE(
            Long userId,
            int amount
    ){

    }
}
