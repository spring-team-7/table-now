package org.example.tablenow.domain.reservation.message.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.RESERVATION_REMINDER_REGISTER_QUEUE;
import static org.example.tablenow.global.constant.RedisKeyConstants.REMINDER_ZSET_KEY;
import static org.example.tablenow.global.constant.TimeConstants.ZONE_ID_ASIA_SEOUL;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderRegisterConsumer {
    private final StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RESERVATION_REMINDER_REGISTER_QUEUE)
    public void handleReminderRegister(ReminderMessage message) {
        try {
            String reservationId = String.valueOf(message.getReservationId());
            double score = message.getRemindAt().atZone(ZONE_ID_ASIA_SEOUL).toEpochSecond();

            redisTemplate.opsForZSet().add(REMINDER_ZSET_KEY, reservationId, score);

            log.info("[ReminderRegisterConsumer] 리마인드 알림 등록 완료 → reservationId={}, remindAt={}",
                    reservationId, message.getRemindAt());
        } catch (Exception e) {
            log.error("[ReminderRegisterConsumer] 리마인드 등록 실패 → message={}", message, e);
        }
    }
}
