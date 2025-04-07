package org.example.tablenow.domain.notification.dto.response;

import lombok.Getter;
import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.notification.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
public class NotificationResponseDto {

  private Long notificationId;
  private NotificationType type;
  private String content;
  private Boolean isRead;
  private LocalDateTime createdAt;

  public NotificationResponseDto(Notification notification) {
    this.notificationId = notification.getId();
    this.type = notification.getType();
    this.content = notification.getContent();
    this.isRead = notification.getIsRead();
    this.createdAt = notification.getCreatedAt();
  }
}
