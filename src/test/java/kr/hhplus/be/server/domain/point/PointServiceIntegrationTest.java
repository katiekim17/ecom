package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        pointRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("userId로 해당 유저가 보유한 포인트를 조회할 수 있다.")
    @Test
    void find() {
        // given
        User savedUser = userRepository.save(User.create("user"));
        pointRepository.save(Point.create(savedUser, 0));
        // when
        Point findPoint = pointService.find(savedUser.getId());
        // then
        assertThat(findPoint.getBalance()).isEqualTo(0);
    }

    @DisplayName("userId와 amount로 해당하는 유저의 포인트를 충전할 수 있다.")
    @Test
    void charge() {
        // given
        User savedUser = userRepository.save(User.create("user"));
        pointRepository.save(Point.create(savedUser, 0));
        int amount = 10;
        PointCommand.CHARGE command = new PointCommand.CHARGE(savedUser.getId(), amount);

        // when
        Point chargedPoint = pointService.charge(command);

        // then
        assertThat(chargedPoint.getBalance()).isEqualTo(amount);
    }

    @DisplayName("userId와 amount로 해당하는 유저의 포인트를 사용할 수 있다.")
    @Test
    void use() {
        // given
        User savedUser = userRepository.save(User.create("user"));
        pointRepository.save(Point.create(savedUser, 10));
        int amount = 10;
        PointCommand.USE command = new PointCommand.USE(savedUser.getId(), amount);
        // when
        Point usedPoint = pointService.use(command);

        // then
        assertThat(usedPoint.getBalance()).isEqualTo(0);
    }

}