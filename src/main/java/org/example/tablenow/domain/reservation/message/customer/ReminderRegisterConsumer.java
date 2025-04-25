package org.example.tablenow.domain.reservation.message.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

import static org.example.tablenow.global.constant.RabbitConstant.RESERVATION_REMINDER_REGISTER_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderRegisterConsumer {
    private final StringRedisTemplate redisTemplate;
    private static final String REMINDER_ZSET_KEY = "reminder:zset";

    @RabbitListener(queues = RESERVATION_REMINDER_REGISTER_QUEUE)
    public void handleReminderRegister(ReminderMessage message) {
        try {
            String reservationId = String.valueOf(message.getReservationId());
            double score = message.getRemindAt().toEpochSecond(ZoneOffset.UTC);

            redisTemplate.opsForZSet().add(REMINDER_ZSET_KEY, reservationId, score);

            log.info("[ReminderRegisterConsumer] 리마인드 알림 등록 완료 → reservationId={}, remindAt={}",
                    reservationId, message.getRemindAt());
        } catch (Exception e) {
            log.error("[ReminderRegisterConsumer] 리마인드 등록 실패 → message={}", message, e);
        }
    }
}
