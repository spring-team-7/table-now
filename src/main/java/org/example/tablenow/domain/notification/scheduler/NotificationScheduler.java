package org.example.tablenow.domain.notification.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
  private final ReservationRepository reservationRepository;
  private final WaitlistRepository waitlistRepository;
  private final NotificationService notificationService;
  private final StoreRepository storeRepository;
  private final ReservationService reservationService;

  // 예약 24시간전 리마인더 알림전송
  @Scheduled(cron = "0 0 * * * * ")
  @Transactional
  public void sendReminderNotifications() {
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
    LocalDateTime reservationDatetime = now.plusDays(1);

    List<Reservation> reservations = reservationRepository.findAllByReservedAtBetween(
        reservationDatetime.minusMinutes(1),
        reservationDatetime.plusMinutes(1)
    );

    for (Reservation reservation : reservations) {
      User user = reservation.getUser();


      if (Boolean.TRUE.equals(user.getIsAlarmEnabled())) {
        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
            .userId(reservation.getUser().getId())
            .storeId(reservation.getStore().getId())
            .type(NotificationType.REMIND)
            .content(String.format("내일 %s 가게 방문예정일 입니다.", reservation.getStore().getName()))
            .build();

        notificationService.createNotification(notificationRequestDto);

      }
    }
  }

  // 빈자리 체크 알림 전송
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void sendVacancyNotifications() {
    List<Store> stores = storeRepository.findAll();

    for (Store store : stores) {
      if (reservationService.hasVacancy(store.getId())) {
        List<Waitlist> waitlists = waitlistRepository.findAllByStoreAndIsNotifiedFalse(store);

        for (Waitlist waitlist : waitlists) {
          User user = waitlist.getUser();
          if (Boolean.TRUE.equals(user.getIsAlarmEnabled())) {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                .userId(waitlist.getUser().getId())
                .storeId(store.getId())
                .type(NotificationType.VACANCY)
                .content(String.format(" %s 가게 빈자리가 생겼습니다.", store.getName()))
                .build();

            notificationService.createNotification(dto);

          }

        }
      }
    }
  }
}