package org.example.tablenow.global.exception;

public class BadRequestException extends HandledException {
    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
