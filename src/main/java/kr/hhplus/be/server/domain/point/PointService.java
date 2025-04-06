package kr.hhplus.be.server.domain.point;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    public Point find(Long userId) {
        return pointRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
    }
}
