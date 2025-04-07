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

    // CATEGORY
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 카테고리를 찾을 수 없습니다."),

    // STORE
    STORE_EXCEED_MAX(HttpStatus.BAD_REQUEST, "등록 가게 수를 초과하였습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 가게를 찾을 수 없습니다."),
    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 가게의 요청 권한이 없습니다."),
    STORE_BAD_REQUEST_TIME(HttpStatus.BAD_REQUEST, "시작시간은 종료시간보다 이전이어야 합니다."),

    // RESERVATION
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 예약에 접근할 수 있는 권한이 없습니다."),
    RESERVATION_DUPLICATE(HttpStatus.CONFLICT, "해당 시간에는 이미 예약이 존재합니다."),
    RESERVATION_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 예약입니다."),
    RESERVATION_STATUS_ALREADY_SET(HttpStatus.BAD_REQUEST, "이미 설정된 예약 상태입니다."),
    RESERVATION_TIME_INVALID(HttpStatus.BAD_REQUEST, "예약 시간은 정각 또는 30분 단위여야 합니다."),
    RESERVATION_STATUS_INVALID(HttpStatus.BAD_REQUEST, "예약 상태가 유효하지 않습니다."),


    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
