package org.example.tablenow.domain.reservation;

import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.example.tablenow.domain.reservation.message.producer.ReminderSendProducer;
import org.example.tablenow.domain.reservation.service.ReminderScheduler;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReminderSchedulerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ReminderSendProducer sendProducer;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReminderScheduler reminderScheduler;

    Long userId = 1L;
    Long storeId = 10L;
    AuthUser authUser = new AuthUser(userId, "user@test.com", UserRole.ROLE_USER, "일반회원");
    User user = User.fromAuthUser(authUser);

    Store store = Store.builder()
            .id(storeId)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(22, 0))
            .capacity(20)
            .build();

    LocalDateTime reservedAt = LocalDateTime.of(2025, 4, 10, 10, 0);

    Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = createReservation(1L, reservedAt, ReservationStatus.RESERVED);
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
    }

    private Reservation createReservation(Long id, LocalDateTime time, ReservationStatus status) {
        Reservation reservation = Reservation.builder()
                .id(id)
                .user(user)
                .store(store)
                .reservedAt(time)
                .build();
        ReflectionTestUtils.setField(reservation, "status", status);
        return reservation;
    }

    @Nested
    class 스케줄러_실행 {
        @Test
        void 알림_도래_예약이_있으면_메시지_발행됨() {
            // given
            String reservationId = "1";
            Set<String> due = Set.of(reservationId);

            given(zSetOperations.rangeByScore(anyString(), anyDouble(), anyDouble())).willReturn(due);
            given(reservationService.getReservationWithStore(1L)).willReturn(reservation);

            // when
            reminderScheduler.pollAndSend();

            // then
            verify(sendProducer).send(any(ReminderMessage.class));
            verify(redisTemplate.opsForZSet()).remove("reminder:zset", reservationId);
        }

        @Test
        void 도래_알림이_비어있으면_아무_작업도_하지_않음() {
            // given
            given(zSetOperations.rangeByScore(anyString(), anyDouble(), anyDouble())).willReturn(Collections.emptySet());

            // when
            reminderScheduler.pollAndSend();

            // then
            verify(sendProducer, never()).send(any());
            verify(zSetOperations, never()).remove(anyString(), any());
        }

        @Test
        void 도래_알림이_null이면_아무_작업도_하지_않음() {
            // given
            given(zSetOperations.rangeByScore(anyString(), anyDouble(), anyDouble())).willReturn(null);

            // when
            reminderScheduler.pollAndSend();

            // then
            verify(sendProducer, never()).send(any());
            verify(zSetOperations, never()).remove(anyString(), any());
        }
    }

}
