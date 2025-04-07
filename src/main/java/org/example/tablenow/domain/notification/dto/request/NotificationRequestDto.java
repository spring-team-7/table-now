package org.example.tablenow.domain.notification.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.notification.enums.NotificationType;

@Getter
@NoArgsConstructor
public class NotificationRequestDto {
  private Long userId;
  private NotificationType type;
  private String content;
}
