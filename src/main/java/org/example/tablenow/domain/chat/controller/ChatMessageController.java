package org.example.tablenow.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.chat.dto.request.ChatMessageRequest;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.chat.service.ChatMessageService;
import org.example.tablenow.global.constant.WebSocketConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Tag(name = "채팅메시지 API")
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${chat.broker}")
    private String brokerType;

    @Operation(summary = "채팅 메시지 전송")
    @MessageMapping("/chat/message")
    public void handleMessage(@Payload @Valid ChatMessageRequest request,
                              SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        log.info("[WebSocket] 현재 세션 Attribute: {}", sessionAttributes);
        if (sessionAttributes == null || !(sessionAttributes.get("userId") instanceof Long userId)) {
            log.warn("[WebSocket] 사용자 정보 없음 (sessionAttributes 또는 userId 누락)");
            return;
        }

        log.info("[WebSocket] 클라이언트 → 서버 메시지 수신 - reservationId: {}, senderId: {}, content: {}",
                request.getReservationId(), userId, request.getContent());

        // 메시지 저장 + 알림 발행
        ChatMessageResponse savedMessage = chatMessageService.saveMessageAndNotify(request, userId);

        // 구독 채널로 메시지 브로드캐스트
        if ("simple".equalsIgnoreCase(brokerType)) {
            // [SimpleBroker] 서버 메모리 브로커 사용
            messagingTemplate.convertAndSend(
                    WebSocketConstants.TOPIC_CHAT_PREFIX_SIMPLE + savedMessage.getReservationId(),
                    savedMessage
            );
        } else if ("rabbit".equalsIgnoreCase(brokerType)) {
            // [RabbitMQ Relay] MQ로 relay
            messagingTemplate.convertAndSend(
                    WebSocketConstants.TOPIC_CHAT_PREFIX_RELAY + savedMessage.getReservationId(),       // RabbitMQ Relay
                    savedMessage
            );
        } else {
            throw new HandledException(ErrorCode.UNSUPPORTED_CHAT_BROKER_TYPE);
        }
    }
}
