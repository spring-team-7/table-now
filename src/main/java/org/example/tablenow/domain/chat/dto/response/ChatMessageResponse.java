package org.example.tablenow.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

import static org.example.tablenow.global.constant.TimeConstants.TIME_YYYY_MM_DD_HH_MM_SS;

@Getter
@Builder
public class ChatMessageResponse {
    private final Long id;
    private final Long senderId;
    private final String senderName;
    private final String content;
    private final String imageUrl;
    @JsonFormat(pattern = TIME_YYYY_MM_DD_HH_MM_SS)
    private final LocalDateTime createdAt;
    @JsonProperty("isRead")
    private final boolean read;

    private final Long reservationId;
    private Long ownerId;
    private Long reservationUserId;

    public static ChatMessageResponse fromChatMessage(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .senderId(chatMessage.getSender().getId())
                .senderName(chatMessage.getSender().getName())
                .content(chatMessage.getContent())
                .imageUrl(chatMessage.getImageUrl())
                .createdAt(chatMessage.getCreatedAt())
                .read(chatMessage.isRead())
                .reservationId(chatMessage.getReservationId())
                .ownerId(chatMessage.getOwnerId())
                .reservationUserId(chatMessage.getReservationUserId())
                .build();
    }
}
