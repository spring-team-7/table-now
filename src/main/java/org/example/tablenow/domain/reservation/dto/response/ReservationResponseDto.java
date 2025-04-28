package org.example.tablenow.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime reservedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime remindAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedAt;
    private final ReservationStatus status;

    @Builder
    private ReservationResponseDto(Long reservationId, Long storeId, String storeName, LocalDateTime reservedAt, LocalDateTime remindAt,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, ReservationStatus status) {
        this.reservationId = reservationId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.reservedAt = reservedAt;
        this.remindAt = remindAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static ReservationResponseDto fromReservation(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .storeId(reservation.getStoreId())
                .storeName(reservation.getStoreName())
                .reservedAt(reservation.getReservedAt())
                .remindAt(reservation.getRemindAt())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .status(reservation.getStatus())
                .build();
    }
}
