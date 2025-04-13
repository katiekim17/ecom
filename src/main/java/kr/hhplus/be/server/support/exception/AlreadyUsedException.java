package kr.hhplus.be.server.support.exception;

import org.springframework.http.HttpStatus;

public class AlreadyUsedException extends BusinessException {
    public AlreadyUsedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
