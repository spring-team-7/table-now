package org.example.tablenow.global.constant;

public class RedisKeyConstants {
    // 공통 prefix
    public static final String RESERVATION_PREFIX = "reservation:";
    public static final String EVENT_PREFIX = "event:";
    public static final String LOCK_PREFIX = "lock:";

    // 예약 관련
    public static final String REMINDER_ZSET_KEY = RESERVATION_PREFIX + "reminder:zset";
    public static final String RESERVATION_LOCK_KEY_PREFIX = LOCK_PREFIX + "reservation:";

    // 이벤트 관련
    public static final String EVENT_JOIN_PREFIX = EVENT_PREFIX + "join:";
    public static final String EVENT_LOCK_KEY_PREFIX = LOCK_PREFIX + "event:";
    public static final String EVENT_OPEN_KEY = EVENT_PREFIX + "open:zset";

    // 토큰 관련
    public static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";
    public static final String BLACKLIST_TOKEN_KEY_PREFIX = "blacklistToken:";

    // 정산 관련
    public static final String SETTLEMENT_SCHEDULE_LOCK_KEY = "settlement-schedule-lock";

    private RedisKeyConstants() {}
}
