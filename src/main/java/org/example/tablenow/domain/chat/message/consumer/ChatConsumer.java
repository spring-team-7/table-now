package org.example.tablenow.domain.chat.message.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.CHAT_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatConsumer {
    private final NotificationService notificationService;

    @RabbitListener(queues = CHAT_QUEUE)
    public void consume(ChatMessageResponse chatMessage) {
        if (chatMessage == null) {
            log.warn("[ChatConsumer] 수신한 메시지가 null입니다.");
            return;
        }

        Long receiverId = determineReceiver(chatMessage);
        if (receiverId == null) {
            log.warn("[ChatConsumer] receiverId를 결정할 수 없습니다. chatMessage: {}", chatMessage);
            return;
        }

        try {
            notificationService.createNotification(
                    NotificationRequestDto.builder()
                            .userId(receiverId)
                            .type(NotificationType.CHAT)
                            .content(buildChatContent(chatMessage))
                            .build()
            );

            log.info("[ChatConsumer] 채팅 알림 전송 완료 → receiverId={}, reservationId={}",
                    receiverId, chatMessage.getReservationId());

        } catch (Exception e) {
            log.error("[ChatConsumer] 채팅 알림 처리 중 예외 발생 → chatMessage: {}", chatMessage, e);
            throw new AmqpRejectAndDontRequeueException("[DLQ] 알림 전송 실패 → DLQ로 이동", e);
        }
    }

    private Long determineReceiver(ChatMessageResponse chatMessage) {
        // 채팅방 소유자(ownerId)와 예약자(reservationUserId) 중
        // 메시지 보낸 사람(senderId)을 제외한 사람이 수신자
        if (chatMessage.getSenderId() == null || chatMessage.getOwnerId() == null || chatMessage.getReservationUserId() == null) {
            throw new HandledException(ErrorCode.INVALID_CHAT_MESSAGE_USER);
        }

        if (chatMessage.getSenderId().equals(chatMessage.getOwnerId())) {
            return chatMessage.getReservationUserId();
        } else {
            return chatMessage.getOwnerId();
        }
    }

    private String buildChatContent(ChatMessageResponse chatMessage) {
        return chatMessage.getSenderName()+"에게서 새로운 채팅 메시지가 도착했습니다.";
    }
}
