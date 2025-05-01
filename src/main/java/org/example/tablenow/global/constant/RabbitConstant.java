package org.example.tablenow.global.constant;

public class RabbitConstant {
    // 빈자리 관련 PREFIX
    public static final String VACANCY_PREFIX = "vacancy";

    // 예약 관련 PREFIX
    public static final String RESERVATION_PREFIX = "reservation";
    public static final String RESERVATION_REMINDER_PREFIX = RESERVATION_PREFIX + ".reminder";

    // 이벤트 관련 PREFIX
    public static final String EVENT_PREFIX = "event";
    public static final String EVENT_OPEN_PREFIX = EVENT_PREFIX + ".open";

    // 빈자리 알림
    public static final String VACANCY_EXCHANGE = VACANCY_PREFIX + ".direct";
    public static final String VACANCY_QUEUE = VACANCY_PREFIX + ".queue";
    public static final String VACANCY_ROUTING_KEY = VACANCY_PREFIX + ".key";

    public static final String VACANCY_DLX = VACANCY_PREFIX + ".dlx";
    public static final String VACANCY_DLQ = VACANCY_PREFIX + ".dlq";

    // 예약 리마인드 알림
    public static final String RESERVATION_REMINDER_SEND_EXCHANGE = RESERVATION_REMINDER_PREFIX + ".send.exchange";
    public static final String RESERVATION_REMINDER_SEND_QUEUE = RESERVATION_REMINDER_PREFIX + ".send.queue";
    public static final String RESERVATION_REMINDER_SEND_DLX = RESERVATION_REMINDER_PREFIX + ".send.dlx";
    public static final String RESERVATION_REMINDER_SEND_DLQ = RESERVATION_REMINDER_PREFIX + ".send.dlq";
    public static final String RESERVATION_REMINDER_SEND_RETRY_QUEUE = RESERVATION_REMINDER_PREFIX + ".send.retry.queue";
    public static final String RESERVATION_REMINDER_SEND_RETRY_EXCHANGE = RESERVATION_REMINDER_PREFIX + ".send.retry.exchange";

    public static final String RESERVATION_REMINDER_REGISTER_EXCHANGE = RESERVATION_REMINDER_PREFIX + ".register.exchange";
    public static final String RESERVATION_REMINDER_REGISTER_QUEUE = RESERVATION_REMINDER_PREFIX + ".register.queue";

    // 이벤트 오픈 알림
    public static final String EVENT_OPEN_EXCHANGE = EVENT_OPEN_PREFIX + ".fanout";
    public static final String EVENT_OPEN_QUEUE = EVENT_OPEN_PREFIX + ".queue";

    public static final String STORE_EXCHANGE = "store.exchange";
    public static final String STORE_CREATE = "store.create";
    public static final String STORE_UPDATE = "store.update";
    public static final String STORE_DELETE = "store.delete";
    public static final String STORE_CREATE_QUEUE = "store.create.queue";
    public static final String STORE_UPDATE_QUEUE = "store.update.queue";
    public static final String STORE_DELETE_QUEUE = "store.delete.queue";
    public static final String STORE_DLX = "store.dlx";
    public static final String STORE_CREATE_DLQ = "store.create.dlq";
    public static final String STORE_UPDATE_DLQ = "store.update.dlq";
    public static final String STORE_DELETE_DLQ = "store.delete.dlq";

    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_QUEUE = "chat.queue";
    public static final String CHAT_ROUTING_KEY = "chat.key";
    public static final String CHAT_DLX = "chat.dlx";
    public static final String CHAT_DLQ = "chat.dlq";

    public static final int TTL_MILLIS = 30000;
}
