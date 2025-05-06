package kr.hhplus.be.server.domain.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatsCommandTest {

    @Nested
    class SaveSalesProducts{

        @DisplayName("날짜 값을 입력하지 않고 command를 생성할 수 없다.")
        @Test
        void constructor() {
            // given // when // then
            assertThatThrownBy(() -> {
                StatsCommand.SaveSalesProducts statsCommand = new StatsCommand.SaveSalesProducts(null);
            }).isInstanceOf(IllegalArgumentException.class).hasMessage("날짜 값이 입력되지 않았습니다.");
        }
    }


}