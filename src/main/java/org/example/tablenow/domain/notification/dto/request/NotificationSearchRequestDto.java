package org.example.tablenow.domain.notification.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationSearchRequestDto {
    private Boolean isRead;

    @Positive
    private int page = 1;

    @Positive
    private int size = 10;
}
