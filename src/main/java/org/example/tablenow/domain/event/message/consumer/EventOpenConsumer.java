package org.example.tablenow.domain.event.message.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.domain.event.message.dto.EventOpenMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.example.tablenow.global.constant.RabbitConstant.EVENT_OPEN_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOpenConsumer {
    private final UserService userService;
    private final NotificationService notificationService;

    private static final String EVENT_OPENED_MSG_TEMPLATE = "%s의 이벤트가 오픈되었습니다!";

    @RabbitListener(queues = EVENT_OPEN_QUEUE)
    public void consume(EventOpenMessage message) {
        if (!isValid(message)) return;

        List<User> users = getUsers(message.getEventId());

        for (User user : users) {
            if (Boolean.TRUE.equals(user.getIsAlarmEnabled())) {
                sendNotification(user, message);
            } else {
                log.debug("[EventOpenConsumer] 알림 비활성 유저 → userId={}", user.getId());
            }
        }
    }

    private boolean isValid(EventOpenMessage message) {
        if (message == null || message.getEventId() == null || message.getEventId() <= 0 || message.getStoreId() == null) {
            log.warn("[EventOpenConsumer] 잘못된 메시지 수신 → {}", message);
            return false;
        }
        return true;
    }

    private List<User> getUsers(Long eventId) {
        try {
            return userService.getUsersWithAlarmEnabled();
        } catch (Exception e) {
            log.error("[EventOpenConsumer] 유저 조회 실패 → eventId={}", eventId, e);
            return Collections.emptyList();
        }
    }

    private void sendNotification(User user, EventOpenMessage message) {
        try {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                    .userId(user.getId())
                    .storeId(message.getStoreId())
                    .type(NotificationType.EVENT_OPEN)
                    .content(String.format(EVENT_OPENED_MSG_TEMPLATE, message.getStoreName()))
                    .build();

            notificationService.createNotification(dto);

            log.info("[EventOpenConsumer][Notification] 알림 전송 완료 → userId={}, storeId={}", user.getId(), message.getStoreId());

        } catch (Exception e) {
            log.error("[EventOpenConsumer][Notification] 알림 전송 실패 → userId={}, eventId={}", user.getId(), message.getEventId(), e);
        }
    }
}
