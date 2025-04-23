package kr.hhplus.be.server.interfaces.point;


import jakarta.validation.Valid;
import kr.hhplus.be.server.domain.point.PointCommand;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.interfaces.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointController implements PointDocs{

    private final PointService pointService;

    @GetMapping("/api/v1/points")
    public ResponseEntity<PointResponse> point(
            @CurrentUser User user
            ) {
        return ResponseEntity.ok(PointResponse.from(pointService.find(user)));
    }

    @PostMapping("/api/v1/points")
    public ResponseEntity<PointResponse> charge(@CurrentUser User user,
                                                @Valid @RequestBody PointRequest.Charge request) {
        PointCommand.Charge command = request.toCommand(user);
        return ResponseEntity.ok(PointResponse.from(pointService.charge(command)));
    }

}
