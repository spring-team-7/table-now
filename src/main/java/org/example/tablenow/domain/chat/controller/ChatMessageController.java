package org.example.tablenow.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.chat.dto.request.ChatMessageRequest;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.chat.service.ChatMessageService;
import org.example.tablenow.global.constant.WebSocketConstants;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message")
    public void handleMessage(@Payload @Valid ChatMessageRequest request,
                              SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null || sessionAttributes.get("userId") == null) {
            log.warn("[WebSocket] 사용자 정보 없음 (sessionAttributes 또는 userId 누락)");
            return;
        }
        Long userId = (Long) sessionAttributes.get("userId");

        // 메시지 저장 + 알림 발행
        ChatMessageResponse savedMessage = chatMessageService.saveMessageAndNotify(request, userId);

        // 구독 채널로 메시지 브로드캐스트
        messagingTemplate.convertAndSend(
                //WebSocketConstants.TOPIC_CHAT_PREFIX_SIMPLE + savedMessage.getReservationId(),    // SimpleBroker: 성능비교를 위해 유지
                WebSocketConstants.TOPIC_CHAT_PREFIX_RELAY + savedMessage.getReservationId(),       // RabbitMQ Relay
                savedMessage
        );
    }
}
