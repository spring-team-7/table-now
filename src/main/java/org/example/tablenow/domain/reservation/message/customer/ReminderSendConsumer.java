package org.example.tablenow.domain.reservation.message.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.reservation.message.dto.ReminderMessage;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.service.UserService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.RESERVATION_REMINDER_SEND_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderSendConsumer {
    private final NotificationService notificationService;
    private final UserService userService;

    private static final String RESERVATION_REMINDER_MSG_TEMPLATE = "%s에 %s 예약이 있습니다!";

    @RabbitListener(queues = RESERVATION_REMINDER_SEND_QUEUE)
    public void consume(ReminderMessage message) {
        if (!isValid(message)) return;
        User user = userService.getUser(message.getUserId());
        sendNotification(user, message);
        log.info("[ReminderSendConsumer] 리마인드 알림 전송 완료 → reservationId={}", message.getReservationId());
    }

    private boolean isValid(ReminderMessage message) {
        if (message == null || message.getUserId() == null || message.getStoreId() == null || message.getReservationId() == null) {
            log.warn("[ReminderSendConsumer] 잘못된 메시지 수신 → {}", message);
            return false;
        }
        return true;
    }

    private void sendNotification(User user, ReminderMessage message) {
        try {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                    .userId(user.getId())
                    .storeId(message.getStoreId())
                    .type(NotificationType.REMIND)
                    .content(String.format(RESERVATION_REMINDER_MSG_TEMPLATE, message.getReservedAt(), message.getStoreName()))
                    .build();

            notificationService.createNotification(dto);

            log.info("[ReminderSendConsumer][Notification] 알림 전송 완료 → userId={}, reservationId={}", user.getId(), message.getReservationId());

        } catch (Exception e) {
            log.error("[ReminderSendConsumer][Notification] 알림 전송 실패 → userId={}, reservationId={}", user.getId(), message.getReservationId(), e);
            throw new AmqpRejectAndDontRequeueException("[DLQ] 알림 전송 실패 → DLQ로 이동", e);
        }
    }
}
