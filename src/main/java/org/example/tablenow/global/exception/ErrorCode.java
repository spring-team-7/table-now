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

    // AUTH
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN_OWNER(HttpStatus.UNAUTHORIZED, "RefreshToken의 소유자가 일치하지 않습니다."),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 권한입니다."),
    OAUTH_PROVIDER_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "OAuth 제공자의 서버에서 오류가 발생했습니다."),
    OAUTH_PROVIDER_UNREACHABLE(HttpStatus.SERVICE_UNAVAILABLE, "OAuth 제공자 서버에 연결할 수 없습니다."),
    OAUTH_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 토큰 파싱에 실패했습니다."),
    OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "OAuth 토큰 요청에 실패했습니다."),
    OAUTH_USER_INFO_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 사용자 정보 요청에 실패했습니다."),
    OAUTH_USER_UNLINK_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "OAuth 사용자 연결 해제 요청에 실패했습니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "요청 쿠키에 리프레시 토큰이 존재하지 않습니다."),
    UNSUPPORTED_SOCIAL_USER_OPERATION(HttpStatus.FORBIDDEN, "소셜 로그인 유저는 해당 기능을 사용할 수 없습니다."),

    // USER
    ALREADY_DELETED_USER(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일 입니다."),
    PASSWORD_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 필수 입력값입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),

    // CATEGORY
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 카테고리를 찾을 수 없습니다."),
    CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다."),

    // STORE
    STORE_EXCEED_MAX(HttpStatus.BAD_REQUEST, "등록 가게 수를 초과하였습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 가게를 찾을 수 없습니다."),
    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 가게의 요청 권한이 없습니다."),
    STORE_BAD_REQUEST_TIME(HttpStatus.BAD_REQUEST, "시작시간은 종료시간보다 이전이어야 합니다."),
    STORE_CLOSED_TIME(HttpStatus.BAD_REQUEST, "가게 영업시간이 아닙니다."),
    STORE_RANKING_TIME_KEY_ERROR(HttpStatus.BAD_REQUEST, "시간 집계 키는 yyyyMMdd 또는 yyyyMMddHH 형식이어야 합니다."),
    STORE_TABLE_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "해당 가게의 하루 수용 가능한 테이블 수를 초과했습니다."),

    // RATING
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "평점이 존재하지 않습니다."),
    RATING_RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "평점 등록 가능한 예약 이력을 찾을 수 없습니다."),
    RATING_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 가게에 대한 평점이 이미 존재합니다."),

    // RESERVATION
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 예약에 접근할 수 있는 권한이 없습니다."),
    RESERVATION_DUPLICATE(HttpStatus.CONFLICT, "해당 시간에는 이미 예약이 존재합니다."),
    RESERVATION_TIME_INVALID(HttpStatus.BAD_REQUEST, "예약 시간은 정각 또는 30분 단위여야 합니다."),
    RESERVATION_STATUS_INVALID(HttpStatus.BAD_REQUEST, "예약 상태가 유효하지 않습니다."),
    RESERVATION_STATUS_UPDATE_FORBIDDEN(HttpStatus.BAD_REQUEST, "예약 상태에서만 변경할 수 있습니다."),
    RESERVATION_LOCK_TIMEOUT(HttpStatus.CONFLICT, "예약 신청 대기 중 시간이 초과되었습니다."),

    // EVENT
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트가 존재하지 않습니다."),
    EVENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 시간에 이벤트가 존재합니다."),
    INVALID_EVENT_STATUS(HttpStatus.BAD_REQUEST, "현재 상태에서는 해당 작업을 수행할 수 없습니다."),
    EVENT_NOT_OPENED(HttpStatus.BAD_REQUEST, "현재 신청할 수 있는 이벤트가 아닙니다."),
    EVENT_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 해당 이벤트에 참가하였습니다."),
    EVENT_FULL(HttpStatus.BAD_REQUEST, "이벤트 정원이 초과되었습니다."),
    EVENT_LOCK_TIMEOUT(HttpStatus.CONFLICT, "이벤트 신청 대기 중 시간이 초과되었습니다."),

    // NOTIFICATION
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림이 존재하지 않습니다."),
    NOTIFICATION_DISABLED(HttpStatus.FORBIDDEN, "알림 수신을 거부한 사용자입니다."),
    NOTIFICATION_MISMATCH(HttpStatus.FORBIDDEN, "알람을 받은 본인만 읽음 처리를 할 수 있습니다."),
    NOTIFICATION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "가게 아이디 입력 필수입니다."),

    // WAITLIST
    WAITLIST_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 대기중인 사용자 입니다."),
    WAITLIST_FULL(HttpStatus.CONFLICT, "대기 정원이 꽉 찼습니다."),
    WAITLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "대기 정보가 존재하지 않습니다."),
    WAITLIST_NOT_ALLOWED(HttpStatus.CONFLICT, "빈자리가 있어 대기 등록이 불가능합니다."),
    WAITLIST_REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "요청 시간이 초과되었습니다."),
    WAITLIST_REQUEST_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "대기 등록 중 요청이 인터럽트되었습니다."),

    // IMAGE
    INVALID_IMAGE_DOMAIN(HttpStatus.BAD_REQUEST, "잘못된 이미지 도메인입니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "잘못된 이미지 URL 입니다."),
    S3_PRESIGNED_URL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 중 오류가 발생했습니다."),

    // COMMON
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "정렬 필드가 잘못되었습니다."),
    INVALID_ORDER_VALUE(HttpStatus.BAD_REQUEST, "정렬 옵션이 잘못되었습니다. 오름차순(asc) 또는 내림차순(desc)을 선택해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

    // PAYMENT
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제가 존재하지 않습니다."),
    ALREADY_PAID(HttpStatus.BAD_REQUEST, "이미 결제가 완료된 예약입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 예약 보증금과 일치하지 않습니다."),
    TOSS_PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "Tosspayment에서 결제가 실패했습니다."),
    TOSS_PAYMENT_CANCEL_FAILED(HttpStatus.BAD_REQUEST, "Tosspayment에서 결제 취소가 실패했습니다."),
    PAYMENT_RESERVATION_MISMATCH(HttpStatus.BAD_REQUEST, "예약이 결제와 일치하지 않습니다."),
    ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 결제가 취소된 예약입니다."),
    UNAUTHORIZED_RESERVATION_ACCESS(HttpStatus.UNAUTHORIZED, "본인 예약이 아닙니다."),

    // REDIS
    REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 연결 중 오류가 발생했습니다."),

    // SETTLEMENT
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "정산이 존재하지 않습니다."),
    INVALID_SETTLEMENT_STATUS(HttpStatus.BAD_REQUEST, "정산 완료 상태에서만 취소할 수 있습니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
