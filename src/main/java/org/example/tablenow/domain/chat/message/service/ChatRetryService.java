package org.example.tablenow.domain.chat.message.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.example.tablenow.global.constant.RabbitConstant.VACANCY_EXCHANGE;
import static org.example.tablenow.global.constant.RabbitConstant.VACANCY_ROUTING_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRetryService {

    private final RabbitTemplate rabbitTemplate;

    private static final String RETRY_HEADER = "x-retry-count";
    private static final int MAX_RETRY_COUNT = 3;

    public void process(Message message) {
        try {
            ChatMessageResponse chatMessage = parseMessage(message);
            int retryCount = extractRetryCount(message);

            if (retryCount >= MAX_RETRY_COUNT) {
                log.warn("[ChatDLQ] 재처리 횟수 초과 → reservationId={}, senderId={}, retryCount={}",
                        chatMessage.getReservationId(), chatMessage.getSenderId(),retryCount);
                return;
            }

            resendMessage(chatMessage, retryCount + 1);

        } catch (Exception e) {
            log.error("[ChatDLQ] 재처리 중 예외 발생", e);
        }
    }

    private ChatMessageResponse parseMessage(Message message) {
        Object object = rabbitTemplate.getMessageConverter().fromMessage(message);
        // 수동 퍼블리시일 경우
        if (object instanceof Map map) {
            return ChatMessageResponse.builder()
                    .reservationId(((Number) map.get("reservationId")).longValue())
                    .senderId(((Number) map.get("senderId")).longValue())
                    .ownerId(((Number) map.get("ownerId")).longValue())
                    .reservationUserId(((Number) map.get("reservationUserId")).longValue())
                    .content((String) map.get("content"))
                    .imageUrl((String) map.get("imageUrl"))
                    .senderName((String) map.get("senderName"))
                    .build();
        }

        return (ChatMessageResponse) object;
    }

    private int extractRetryCount(Message message) {
        return (Integer) message.getMessageProperties()
                .getHeaders()
                .getOrDefault(RETRY_HEADER, 0);
    }

    private void resendMessage(ChatMessageResponse chatMessage, int nextRetryCount) {
        MessageProperties props = new MessageProperties();
        props.setHeader(RETRY_HEADER, nextRetryCount);

        Message retryMessage = rabbitTemplate.getMessageConverter().toMessage(chatMessage, props);
        rabbitTemplate.send(VACANCY_EXCHANGE, VACANCY_ROUTING_KEY, retryMessage);

        log.info("[DLQ] 재처리 시도 완료 → reservationId={}, senderId={}, retryCount={}",
                chatMessage.getReservationId(), chatMessage.getSenderId(), nextRetryCount);
    }
}
