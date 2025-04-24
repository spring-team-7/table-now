package org.example.tablenow.domain.reservation.message.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.reservation.entity.Reservation;

import java.time.LocalDateTime;

@Getter
public class ReminderMessage {
    private final Long reservationId;
    private final Long userId;
    private final Long storeId;
    private final String storeName;
    private final LocalDateTime reservedAt;
    private final LocalDateTime remindAt;

    @Builder
    public ReminderMessage(Long reservationId, Long userId, Long storeId, String storeName, LocalDateTime reservedAt, LocalDateTime remindAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.reservedAt = reservedAt;
        this.remindAt = remindAt;
    }

    public static ReminderMessage fromReservation(Reservation reservation) {
        return ReminderMessage.builder()
                .reservationId(reservation.getId())
                .userId(reservation.getUser().getId())
                .storeId(reservation.getStore().getId())
                .storeName(reservation.getStore().getName())
                .reservedAt(reservation.getReservedAt())
                .remindAt(reservation.getRemindAt())
                .build();
    }
}
