package org.example.tablenow.global.constant;

public class RabbitConstant {
    public static final String VACANCY_EXCHANGE = "vacancy.direct";
    public static final String VACANCY_QUEUE = "vacancy.queue";
    public static final String VACANCY_ROUTING_KEY = "vacancy.key";

    public static final String VACANCY_DLX = "vacancy.dlx";
    public static final String VACANCY_DLQ = "vacancy.dlq";

    public static final String EVENT_OPEN_EXCHANGE = "event.open.fanout";
    public static final String EVENT_OPEN_QUEUE = "event.open.queue";

    public static final String RESERVATION_REMINDER_REGISTER_EXCHANGE = "reservation.reminder.register.exchange";
    public static final String RESERVATION_REMINDER_REGISTER_QUEUE = "reservation.reminder.register.queue";

    public static final String RESERVATION_REMINDER_SEND_EXCHANGE = "reservation.reminder.send.exchange";
    public static final String RESERVATION_REMINDER_SEND_QUEUE = "reservation.reminder.send.queue";

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

    public static final int TTL_MILLIS = 30000;
}
