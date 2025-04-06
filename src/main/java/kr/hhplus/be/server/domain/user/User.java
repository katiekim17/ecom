package kr.hhplus.be.server.domain.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class User {

    Long id;
    String name;

    @Builder
    private User(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
