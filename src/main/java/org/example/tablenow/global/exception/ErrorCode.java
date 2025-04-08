package org.example.tablenow.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    AUTHORIZATION(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림이 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    NOTIFICATION_DISABLED(HttpStatus.FORBIDDEN, "알림 수신을 거부한 사용자입니다."),
    NOTIFICATION_MISMATCH(HttpStatus.FORBIDDEN, "알람을 받은 본인만 읽음 처리를 할 수 있습니다."),

    // 대기 등록
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 가게입니다."),
    WAITLIST_ALREADY_REGISTERED(HttpStatus.CONFLICT,"이미 대기중인 사용자 입니다."),
    WAITLIST_FULL(HttpStatus.CONFLICT, "대기 정원이 꽉 찼습니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
