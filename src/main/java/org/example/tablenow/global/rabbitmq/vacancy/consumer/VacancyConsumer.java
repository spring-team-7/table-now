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
import org.example.tablenow.global.rabbitmq.vacancy.dto.VacancyEventDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static org.example.tablenow.global.constant.RabbitConstant.VACANCY_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyConsumer {
    private final WaitlistRepository waitlistRepository;
    private final NotificationService notificationService;
    private final StoreService storeService;
    private final ReservationService reservationService;

    @RabbitListener(queues =VACANCY_QUEUE)
    public void consume(VacancyEventDto event) {
        try {
            Long storeId = event.getStoreId();
            LocalDate waitDate = event.getWaitDate();

            log.info("[VacancyConsumer] MQ 수신 → storeId={}, waitDate={}", storeId, waitDate);

            Store findStore = storeService.getStore(storeId);

            if (!reservationService.hasVacancyDate(findStore, waitDate)) {
                log.info("[VacancyConsumer] 빈자리 없음 → storeId={}, waitDate={}", storeId, waitDate);
                return;
            }

            List<Waitlist> waitlists = waitlistRepository.findWaitingList(findStore, waitDate);

            for (Waitlist waitlist : waitlists) {
                processWaitlist(findStore, waitlist);
            }

        } catch (Exception e) {
            log.error("[VacancyConsumer] MQ 처리 중 예외 발생", e);
        }
    }

    private void processWaitlist(Store store,Waitlist waitlist){
        User findUser = waitlist.getUser();

        if (!Boolean.TRUE.equals(findUser.getIsAlarmEnabled())) {
            log.info("[VacancyConsumer] 알림 비활성 유저 → userId={}", findUser.getId());
            return;
        }

        notifyVacancy(store, waitlist);
    }

    private void notifyVacancy(Store store, Waitlist waitlist) {
        LocalDate waitDate = waitlist.getWaitDate();

        NotificationRequestDto dto = NotificationRequestDto.builder()
            .userId(waitlist.getUser().getId())
            .storeId(store.getId())
            .type(NotificationType.VACANCY)
            .content(vacancyMessage(store.getName(), waitlist.getWaitDate()))
            .build();

        notificationService.createNotification(dto);

        log.info("[VacancyConsumer] 알림 전송 완료 → userId={}, storeId={}, waitDate={}",
            waitlist.getUser().getId(), store.getId(), waitDate);

    }

    private String vacancyMessage(String storeName, LocalDate waitDate){
        return String.format("%s가게 %s에 빈자리가 생겼습니다.", storeName, waitDate);
    }
}