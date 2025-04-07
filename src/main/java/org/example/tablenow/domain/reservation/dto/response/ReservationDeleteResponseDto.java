package org.example.tablenow.domain.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;

@Getter
public class ReservationDeleteResponseDto {
    private final Long reservationId;
    private final LocalDateTime modifiedAt;
    private final ReservationStatus status;

    @Builder
    private ReservationDeleteResponseDto(Long reservationId, LocalDateTime modifiedAt, ReservationStatus status) {
        this.reservationId = reservationId;
        this.modifiedAt = modifiedAt;
        this.status = status;
    }
}
