package org.example.tablenow.global.exception;

public class AuthorizationException extends HandledException {
    public AuthorizationException() {
        super(ErrorCode.AUTHORIZATION);
    }

    public AuthorizationException(String message) {
        super(ErrorCode.AUTHORIZATION, message);
    }
}
