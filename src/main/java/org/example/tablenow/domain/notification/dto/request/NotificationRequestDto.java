package org.example.tablenow.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.notification.enums.NotificationType;

@Getter
@NoArgsConstructor
public class NotificationRequestDto {

    @NotNull(message = "userId를 입력해주세요.")
    private Long userId;

    private Long storeId;

    @NotNull(message = "알림 타입을 입력해주세요.")
    private NotificationType type;

    @NotBlank(message = "알림 내용을 입력해주세요.")
    private String content;

    @Builder
    public NotificationRequestDto(Long userId, Long storeId, NotificationType type, String content) {
        this.userId = userId;
        this.storeId = storeId;
        this.type = type;
        this.content = content;
    }

    public static NotificationRequestDto fromWaitlist(Long userId, Long storeId, NotificationType type, String content) {
        return NotificationRequestDto.builder()
            .userId(userId)
            .storeId(storeId)
            .type(type)
            .content(content)
            .build();
    }
}
