package kr.hhplus.be.server.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Nested
    class charge{

        @DisplayName("포인트를 충전하면 기존 포인트와 충전할 포인트를 합산한 포인트를 보유한다.")
        @Test
        void success() {
            // given
            int amount = 1;
            int originalAmount = 10;
            Point point = Point.builder().balance(originalAmount).build();

            // when
            point.charge(amount);

            // then
            assertThat(point.getBalance()).isEqualTo(amount + originalAmount);
        }

        @DisplayName("포인트 충전을 1원보다 작게 충전하는 경우 IllegalArgumentException가 발생한다.")
        @Test
        void fail() {
            // given
            int amount = 0;
            Point point = Point.builder().balance(0).build();

            // when // then
            assertThatThrownBy(() -> point.charge(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("포인트는 1포인트 이상부터 충전이 가능합니다.");
        }

        @DisplayName("포인트 충전 이후 잔고가 10,000,000원 이상인 경우 IllegalArgumentException이 발생한다.")
        @Test
        void fail2() {
            // given
            int amount = 10_000_001;
            Point point = Point.builder().balance(0).build();

            // when // then
            assertThatThrownBy(() -> point.charge(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("충전 이후 포인트는 10,000,000포인트를 넘을 수 없습니다.");
        }
    }

}