package org.example.tablenow.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private final String senderName;
    private final String content;
    private final String imageUrl;
    private final LocalDateTime createdAt;

    public static ChatMessageResponse fromChatMessage(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .senderName(chatMessage.getSender().getName())
                .content(chatMessage.getContent())
                .imageUrl(chatMessage.getImageUrl())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
