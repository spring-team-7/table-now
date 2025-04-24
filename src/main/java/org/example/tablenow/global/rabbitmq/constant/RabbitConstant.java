package org.example.tablenow.global.rabbitmq.constant;

public class RabbitConstant {
    public static final String VACANCY_EXCHANGE = "vacancy.direct";
    public static final String VACANCY_QUEUE = "vacancy.queue";
    public static final String VACANCY_ROUTING_KEY = "vacancy.key";

    public static final String EVENT_OPEN_EXCHANGE = "event.open.fanout";
    public static final String EVENT_OPEN_QUEUE = "event.open.queue";
}
