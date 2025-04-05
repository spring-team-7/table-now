package org.example.tablenow.domain.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Getter
public class ReservationResponseDto {
    private final Long reservationId;
    private final Long storeId;
//    private final String storeName;
    private final LocalDateTime reservedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final ReservationStatus status;

    @Builder
    private ReservationResponseDto(Long reservationId, Long storeId, /* String storeName, */ LocalDateTime reservedAt,
                                   LocalDateTime createdAt, LocalDateTime modifiedAt, ReservationStatus status) {
        this.reservationId = reservationId;
        this.storeId = storeId;
//        this.storeName = storeName;
        this.reservedAt = reservedAt;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.status = status;
    }
}
