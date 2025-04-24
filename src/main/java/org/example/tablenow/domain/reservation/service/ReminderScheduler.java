package org.example.tablenow.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.example.tablenow.domain.reservation.message.producer.ReminderSendProducer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private static final String ZSET_KEY = "reminder:zset";
    private final StringRedisTemplate redisTemplate;
    private final ReminderSendProducer sendProducer;
    private final ReservationService reservationService;

    @Scheduled(fixedRateString = "60000")
    public void pollAndSend() {
        long now = Instant.now().getEpochSecond();
        Set<String> due = redisTemplate.opsForZSet().rangeByScore(ZSET_KEY, 0, now);
        if (due == null || due.isEmpty()) return;

        for (String id : due) {
            try {
                sendProducer.send(buildMessage(id));
                redisTemplate.opsForZSet().remove(ZSET_KEY, id);
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