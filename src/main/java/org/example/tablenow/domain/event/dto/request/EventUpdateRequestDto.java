package org.example.tablenow.domain.event.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventUpdateRequestDto {
    @Future(message = "openAt은 미래 시각이어야 합니다.")
    private LocalDateTime openAt;
    @Future(message = "eventTime은 미래 시각이어야 합니다.")
    private LocalDateTime eventTime;
    @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다.")
    private Integer limitPeople;
}
