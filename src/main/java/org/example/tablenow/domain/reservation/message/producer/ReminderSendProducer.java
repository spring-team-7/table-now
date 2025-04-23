package org.example.tablenow.domain.reservation.message.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.rabbitmq.constant.RabbitConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderSendProducer {
    private final RabbitTemplate rabbitTemplate;

    public void send(ReminderMessage message) {
        rabbitTemplate.convertAndSend(
                RESERVATION_REMINDER_SEND_EXCHANGE,
                RESERVATION_REMINDER_SEND_ROUTING_KEY,
                message
        );

        log.info("[ReminderRegisterProducer] 리마인드 알림 발행 메시지 발행 완료 → userId={}, reservationId={}, remindAt={}",
                message.getUserId(),
                message.getReservationId(),
                message.getRemindAt());
    }
}
