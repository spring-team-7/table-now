package org.example.tablenow.domain.waitlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
public class WaitlistRequestDto {
    @NotNull(message = "가게 ID를 입력하세요.")
    private Long storeId;

    @NotNull(message = "대기 희망 날짜를 입력하세요.")
    private LocalDate waitDate;
}
