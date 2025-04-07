package org.example.tablenow.domain.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Getter
public class ReservationResponseDto {
    private final Long reservationId;
    private final Long storeId;
    private final String storeName;
    private final LocalDateTime reservedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final ReservationStatus status;

    @Builder
    private ReservationResponseDto(Long reservationId, Long storeId, String storeName, LocalDateTime reservedAt,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, ReservationStatus status) {
        this.reservationId = reservationId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.reservedAt = reservedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static ReservationResponseDto fromReservation(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .storeId(reservation.getStore().getId())
                .storeName(reservation.getStore().getName())
                .reservedAt(reservation.getReservedAt())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .status(reservation.getStatus())
                .build();
    }
}
