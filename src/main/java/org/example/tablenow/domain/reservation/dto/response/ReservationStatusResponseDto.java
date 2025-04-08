package org.example.tablenow.domain.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Getter
public class ReservationStatusResponseDto {
    private final Long reservationId;
    private final LocalDateTime updatedAt;
    private final ReservationStatus status;

    @Builder
    private ReservationStatusResponseDto(Long reservationId, LocalDateTime updatedAt, ReservationStatus status) {
        this.reservationId = reservationId;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static ReservationStatusResponseDto fromReservation(Reservation reservation) {
        return ReservationStatusResponseDto.builder()
                .reservationId(reservation.getId())
                .updatedAt(reservation.getUpdatedAt())
                .status(reservation.getStatus())
                .build();
    }
}
