package kr.hhplus.be.server.interfaces.point;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointRequest(
) {
        public record Charge(
                @Positive
                @NotNull
                Integer amount
        ){

        }
}
