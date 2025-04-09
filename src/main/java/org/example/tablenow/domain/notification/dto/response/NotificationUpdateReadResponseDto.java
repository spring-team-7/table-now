package org.example.tablenow.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.notification.entity.Notification;

@Getter
public class NotificationUpdateReadResponseDto {

  private final Long notificationId;
  private final Boolean isRead;

  @Builder
  public NotificationUpdateReadResponseDto(Long notificationId, Boolean isRead) {
    this.notificationId = notificationId;
    this.isRead = isRead;
  }

  public static NotificationUpdateReadResponseDto from(Notification notification) {
    return NotificationUpdateReadResponseDto.builder()
        .notificationId(notification.getId())
        .isRead(notification.getIsRead())
        .build();
  }
}

