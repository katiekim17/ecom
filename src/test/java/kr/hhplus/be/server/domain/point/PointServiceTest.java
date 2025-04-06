package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @Nested
    class find{
        @DisplayName("userId에 해당하는 유저의 포인트를 조회할 수 있다.")
        @Test
        void success() {
            // given
            Long userId = 1L;
            User user = User.builder().id(userId).name("yeop").build();
            when(pointRepository.findById(userId))
                    .thenReturn(Optional.of(Point.builder().user(user).balance(0).build()));

            // when
            Point point = pointService.find(userId);

            // then
            assertThat(point.user).isEqualTo(user);
            assertThat(point.balance).isEqualTo(0);
            verify(pointRepository, times(1)).findById(userId);
        }

        @DisplayName("userId에 해당하는 유저가 없는 경우 IllegalArgumentException이 발생한다.")
        @Test
        void fail() {
            // given
            Long userId = 1L;

            // when // then
            assertThatThrownBy(() -> pointService.find(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("등록되지 않은 회원입니다.");

            verify(pointRepository, times(1)).findById(userId);
        }
    }

}