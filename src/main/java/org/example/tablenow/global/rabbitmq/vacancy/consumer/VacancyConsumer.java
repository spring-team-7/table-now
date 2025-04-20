package org.example.tablenow.global.rabbitmq.vacancy.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.rabbitmq.config.RabbitConfig;
import org.example.tablenow.global.rabbitmq.vacancy.dto.VacancyEventDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyConsumer {
    private final WaitlistRepository waitlistRepository;
    private final NotificationService notificationService;
    private final StoreService storeService;
    private final ReservationService reservationService;

    @RabbitListener(queues = RabbitConfig.VACANCY_QUEUE)
    public void consume(VacancyEventDto vacancyEventDto) {
        Long storeId = vacancyEventDto.getStoreId();
        LocalDate waitDate = vacancyEventDto.getWaitDate();

        log.info("[VacancyConsumer] MQ 수신 → storeId={}, waitDate={}", storeId, waitDate);

        Store findStore = storeService.getStore(storeId);
        List<Waitlist> waitlists = waitlistRepository.findAllWithUserByStoreAndWaitDateAndIsNotifiedFalse(findStore, waitDate);
        for (Waitlist waitlist : waitlists) {
            User findUser = waitlist.getUser();
            if (!Boolean.TRUE.equals(findUser.getIsAlarmEnabled())) continue;
            if (!reservationService.hasVacancyDate(findStore, waitDate)) continue;

            notifyVacancy(findStore, waitlist);
        }
    }

    private void notifyVacancy(Store store, Waitlist waitlist) {
        LocalDate waitDate = waitlist.getWaitDate();
        NotificationRequestDto dto = NotificationRequestDto.builder()
            .userId(waitlist.getUser().getId())
            .storeId(store.getId())
            .type(NotificationType.VACANCY)
            .content(String.format("%s가게에서 %s에 빈자리가 생겼습니다.",
                store.getName(),
                waitDate.toString()))
            .build();

        notificationService.createNotification(dto);

        log.info("[VacancyConsumer] 알림 전송 완료 → userId={}, storeId={}, waitDate={}",
            waitlist.getUser().getId(), store.getId(), waitDate);

    }
}