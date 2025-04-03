package org.example.tablenow.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    AUTHORIZATION(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
