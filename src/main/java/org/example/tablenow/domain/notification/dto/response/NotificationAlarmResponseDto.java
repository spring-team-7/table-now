package org.example.tablenow.domain.notification.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationAlarmResponseDto {
  boolean isAlarmEnabled;
  LocalDateTime updatedAt;

  public NotificationAlarmResponseDto(boolean isAlarmEnabled, LocalDateTime updatedAt) {
    this.isAlarmEnabled = isAlarmEnabled;
    this.updatedAt = updatedAt;
  }


}
