package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

@Getter
@Setter
public class ReservationSearchRequest {
    private ReservationStatus status;
    @Positive
    private int page = 1;
    @Positive
    private int size = 10;
}
