package org.example.tablenow.domain.event.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {
    @NotNull(message = "storeId는 필수입니다.")
    private Long storeId;
    @Size(max = 500, message = "내용은 500자 이하로 입력해주세요.")
    private String content;
    @NotNull
    @Future(message = "openAt은 미래 시각이어야 합니다.")
    private LocalDateTime openAt;
    @NotNull
    @Future(message = "endAt은 미래 시각이어야 합니다.")
    private LocalDateTime endAt;
    @NotNull
    @Future(message = "eventTime은 미래 시각이어야 합니다.")
    private LocalDateTime eventTime;
    @NotNull
    @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다.")
    private Integer limitPeople;
}
