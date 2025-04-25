package org.example.tablenow.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private final Long id;
    private final Long senderId;
    private final String senderName;
    private final String content;
    private final String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;
    @JsonProperty("isRead")
    private final boolean read;

    public static ChatMessageResponse fromChatMessage(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .senderId(chatMessage.getSender().getId())
                .senderName(chatMessage.getSender().getName())
                .content(chatMessage.getContent())
                .imageUrl(chatMessage.getImageUrl())
                .createdAt(chatMessage.getCreatedAt())
                .read(chatMessage.isRead())
                .build();
    }
}
