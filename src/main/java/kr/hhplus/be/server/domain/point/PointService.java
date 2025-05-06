package kr.hhplus.be.server.domain.point;


import kr.hhplus.be.server.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Point find(User user) {
        return pointRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)      // 100ms → 200ms → 400ms
    )
    @Transactional
    public Point charge(PointCommand.Charge command) {
        Point point = find(command.user());
        point.charge(command.amount());

        return point;
    }

    @Transactional
    public Point use(PointCommand.Use command) {
        User user = command.user();
        Point point = pointRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));

        point.use(command.amount());
        return point;
    }
}
