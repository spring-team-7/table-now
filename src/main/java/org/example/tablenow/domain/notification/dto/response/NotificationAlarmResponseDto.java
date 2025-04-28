package org.example.tablenow.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
public class NotificationAlarmResponseDto {
    private final boolean isAlarmEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedAt;

    @Builder
    public NotificationAlarmResponseDto(boolean isAlarmEnabled, LocalDateTime updatedAt) {
        this.isAlarmEnabled = isAlarmEnabled;
        this.updatedAt = updatedAt;
    }

    public static NotificationAlarmResponseDto fromNotification(User user) {
        return NotificationAlarmResponseDto.builder()
            .isAlarmEnabled(user.getIsAlarmEnabled())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}

