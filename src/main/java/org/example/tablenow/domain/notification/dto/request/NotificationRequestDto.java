package org.example.tablenow.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.notification.enums.NotificationType;

@Getter
@Builder
public class NotificationRequestDto {

  @NotNull(message = "userId를 입력해주세요.")
  private Long userId;

  // 스토아이디는 널일수도 있음.
  private Long storeId;

  @NotNull(message = "알림 타입을 입력해주세요.")
  private NotificationType type;

  @NotBlank(message = "알림 내용을 입력해주세요.")
  private String content;
}
