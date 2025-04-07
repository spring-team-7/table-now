package org.example.tablenow.domain.notification.dto.response;

import lombok.Getter;

@Getter
public class NotificationUpdateReadResponseDto {

  private final Long notificationId;
  private final Boolean isRead;

  public NotificationUpdateReadResponseDto(Long notificationId, Boolean isRead) {
    this.notificationId = notificationId;
    this.isRead = isRead;
  }

}
