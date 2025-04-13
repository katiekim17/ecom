package kr.hhplus.be.server.support.exception;

import org.springframework.http.HttpStatus;

public class AlreadyIssuedException extends BusinessException {
    public AlreadyIssuedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
