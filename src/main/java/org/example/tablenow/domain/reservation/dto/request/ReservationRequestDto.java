package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.annotation.HalfHourOnly;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationRequestDto {
    @NotNull
    private Long storeId;
    @NotNull
    @HalfHourOnly
    private LocalDateTime reservedAt;

    @Builder
    public ReservationRequestDto(Long storeId, LocalDateTime reservedAt) {
        this.storeId = storeId;
        this.reservedAt = reservedAt;
    }
}