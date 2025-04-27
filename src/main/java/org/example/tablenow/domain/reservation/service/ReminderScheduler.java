package org.example.tablenow.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.example.tablenow.domain.reservation.message.producer.ReminderSendProducer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

import static org.example.tablenow.global.constant.RedisKeyConstants.REMINDER_ZSET_KEY;
import static org.example.tablenow.global.constant.TimeConstants.ZONE_ID_ASIA_SEOUL;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private final StringRedisTemplate redisTemplate;
    private final ReminderSendProducer sendProducer;
    private final ReservationService reservationService;

    @Scheduled(fixedRateString = "60000")
    public void pollAndSend() {
        long now = LocalDateTime.now()
                .atZone(ZONE_ID_ASIA_SEOUL)
                .toEpochSecond();
        Set<String> due = redisTemplate.opsForZSet().rangeByScore(REMINDER_ZSET_KEY, 0, now);
        if (due == null || due.isEmpty()) return;

        for (String id : due) {
            try {
                sendProducer.send(buildMessage(id));
                redisTemplate.opsForZSet().remove(REMINDER_ZSET_KEY, id);
            } catch (Exception e) {
                log.error("[ReminderScheduler] 알림 발송 실패 → reservationId={}", id, e);
            }
        }
    }

    private ReminderMessage buildMessage(String id) {
        Reservation reservation = reservationService.getReservationWithStore(Long.valueOf(id));
        return ReminderMessage.fromReservation(reservation);
    }
}