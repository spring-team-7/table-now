package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.annotation.HalfHourOnly;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationUpdateRequestDto {
    @NotNull
    @HalfHourOnly
    private LocalDateTime reservedAt;

    @Builder
    public ReservationUpdateRequestDto(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }
}
