package org.example.tablenow.domain.reservation.message.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Async
public class ReminderRegisterProducer {
    private final RabbitTemplate rabbitTemplate;

    public void send(ReminderMessage message) {
        rabbitTemplate.convertAndSend(
                RESERVATION_REMINDER_REGISTER_EXCHANGE,
                RESERVATION_REMINDER_REGISTER_ROUTING_KEY,
                message
        );

        log.info("[ReminderRegisterProducer] 리마인드 알림 등록 메시지 발행 → userId={}, reservationId={}, remindAt={}",
                message.getUserId(),
                message.getReservationId(),
                message.getRemindAt());
    }
}
