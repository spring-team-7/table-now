package org.example.tablenow.global.rabbitmq.constant;

public class RabbitConstant {
    public static final String VACANCY_EXCHANGE = "vacancy.direct";
    public static final String VACANCY_QUEUE = "vacancy.queue";
    public static final String VACANCY_ROUTING_KEY = "vacancy.key";

    public static final String EVENT_OPEN_EXCHANGE = "event.open.fanout";
    public static final String EVENT_OPEN_QUEUE = "event.open.queue";

    public static final String RESERVATION_REMINDER_REGISTER_EXCHANGE = "reservation.reminder.register.exchange";
    public static final String RESERVATION_REMINDER_REGISTER_QUEUE = "reservation.reminder.register.queue";
    public static final String RESERVATION_REMINDER_REGISTER_ROUTING_KEY = "reservation.reminder.register.key";

    public static final String RESERVATION_REMINDER_SEND_EXCHANGE = "reservation.reminder.send.exchange";
    public static final String RESERVATION_REMINDER_SEND_QUEUE = "reservation.reminder.send.queue";
    public static final String RESERVATION_REMINDER_SEND_ROUTING_KEY = "reservation.reminder.send.key";
}
