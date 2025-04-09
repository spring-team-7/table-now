package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationRequestDto {
    @NotNull
    private Long storeId;
    @NotNull
    private LocalDateTime reservedAt;

    @Builder
    private ReservationRequestDto(Long storeId, LocalDateTime reservedAt) {
        this.storeId = storeId;
        this.reservedAt = reservedAt;
    }
}