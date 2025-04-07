package org.example.tablenow.domain.notification.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationAlarmRequestDto {
  private boolean alarmEnabled;

  public NotificationAlarmRequestDto(boolean alarmEnabled) {
    this.alarmEnabled = alarmEnabled;
  }
}
