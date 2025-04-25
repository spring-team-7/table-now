package org.example.tablenow.global.constant;

public class RedisKeyConstants {
    // 예약
    public static final String REMINDER_ZSET_KEY = "reminder:zset";
    public static final String RESERVATION_LOCK_KEY_PREFIX = "lock:reservation";

    // 이벤트
    public static final String EVENT_JOIN_PREFIX = "event:join";
    public static final String EVENT_LOCK_KEY_PREFIX = "lock:event";
    public static final String EVENT_OPEN_KEY = "event:open";

    private RedisKeyConstants() {}
}
