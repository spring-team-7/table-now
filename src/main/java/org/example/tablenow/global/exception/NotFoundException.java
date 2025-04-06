package org.example.tablenow.global.exception;

public class NotFoundException extends HandledException {
    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
