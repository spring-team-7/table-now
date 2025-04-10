package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

@Getter
@NoArgsConstructor
public class ReservationStatusChangeRequestDto {
    @NotNull
    private ReservationStatus status;

    @Builder
    private ReservationStatusChangeRequestDto(ReservationStatus status) {
        this.status = status;
    }
}
