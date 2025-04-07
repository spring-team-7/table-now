package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

@Getter
@NoArgsConstructor
public class ReservationStatusChangeRequestDto {
    @NotNull
    private ReservationStatus status;
}
