package org.example.tablenow.domain.notification.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    // 예약 24시간전 리마인더 알림전송
    @Scheduled(cron = "0 0,30 * * * *")
    @Transactional
    public void sendReminderNotifications() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime targetTime = now.plusDays(1);

        List<Reservation> reservations = reservationRepository.findAllByReservedAtBetween(
            targetTime.minusMinutes(5),
            targetTime.plusMinutes(5)
        );

        for (Reservation reservation : reservations) {
            User user = reservation.getUser();
            if (Boolean.TRUE.equals(user.getIsAlarmEnabled())) {
                notifyReminder(reservation);
            }
        }
    }


    //  예약 리마인더 알림 생성
    private void notifyReminder(Reservation reservation) {
        NotificationRequestDto dto = new NotificationRequestDto(
            reservation.getUser().getId(),
            reservation.getStoreId(),
            NotificationType.REMIND,
            String.format("내일 %s 가게 방문예정일 입니다.", reservation.getStoreName())
        );
        notificationService.createNotification(dto);
    }

}