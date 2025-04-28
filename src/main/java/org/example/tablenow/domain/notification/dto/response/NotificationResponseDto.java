package org.example.tablenow.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.notification.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
public class NotificationResponseDto {

    private final Long notificationId;
    private final NotificationType type;
    private final String content;
    private final Boolean isRead;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @Builder
    public NotificationResponseDto(Long notificationId, NotificationType type, String content, Boolean isRead, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.type = type;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static NotificationResponseDto fromNotification(Notification notification) {
        return NotificationResponseDto.builder()
            .notificationId(notification.getId())
            .type(notification.getType())
            .content(notification.getContent())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}

