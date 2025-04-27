package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatusChangeRequestDto {
    @NotNull
    private ReservationStatus status;
}
