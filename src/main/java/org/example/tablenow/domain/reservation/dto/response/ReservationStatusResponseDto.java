package org.example.tablenow.domain.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationStatusResponseDto {
    private final Long reservationId;
    private final LocalDateTime updatedAt;
    private final ReservationStatus status;

    public static ReservationStatusResponseDto fromReservation(Reservation reservation) {
        return builder()
                .reservationId(reservation.getId())
                .updatedAt(reservation.getUpdatedAt())
                .status(reservation.getStatus())
                .build();
    }
}
