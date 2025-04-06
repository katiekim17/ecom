package kr.hhplus.be.server.interfaces.point;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PointController implements PointDocs{

    @GetMapping("/api/v1/users/{userId}/points")
    public ResponseEntity<PointResponse> point(
            @PathVariable Long userId
            ) {
        return ResponseEntity.ok(new PointResponse(userId, 0));
    }

    @PostMapping("/api/v1/users/{userId}/points")
    public ResponseEntity<PointResponse> charge(@PathVariable Long userId,
                                                @Valid @RequestBody PointRequest.Charge request) {
        return ResponseEntity.ok(new PointResponse(userId, request.amount()));
    }

}
