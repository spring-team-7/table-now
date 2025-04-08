package org.example.tablenow.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    AUTHORIZATION(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다."),
    CONFLICT(HttpStatus.CONFLICT, "요청이 서버의 상태와 충돌했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),

    // AUTH & USER
    ALREADY_DELETED_USER(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일 입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 권한입니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "요청 쿠키에 리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰이 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),

    // CATEGORY
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 카테고리를 찾을 수 없습니다."),
    CATEGORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 카테고리입니다."),

    // STORE
    STORE_EXCEED_MAX(HttpStatus.BAD_REQUEST, "등록 가게 수를 초과하였습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 가게를 찾을 수 없습니다."),
    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 가게의 요청 권한이 없습니다."),
    STORE_BAD_REQUEST_TIME(HttpStatus.BAD_REQUEST, "시작시간은 종료시간보다 이전이어야 합니다."),
    STORE_CLOSED_TIME(HttpStatus.BAD_REQUEST,"가게 영업시간이 아닙니다."),

    // RESERVATION
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 예약에 접근할 수 있는 권한이 없습니다."),
    RESERVATION_DUPLICATE(HttpStatus.CONFLICT, "해당 시간에는 이미 예약이 존재합니다."),
    RESERVATION_TIME_INVALID(HttpStatus.BAD_REQUEST, "예약 시간은 정각 또는 30분 단위여야 합니다."),
    RESERVATION_STATUS_INVALID(HttpStatus.BAD_REQUEST, "예약 상태가 유효하지 않습니다."),
    RESERVATION_STATUS_UPDATE_FORBIDDEN(HttpStatus.BAD_REQUEST, "예약 상태에서만 변경할 수 있습니다."),

    // EVENT
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트가 존재하지 않습니다."),
    EVENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 시간에 이벤트가 존재합니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
