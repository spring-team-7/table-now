package org.example.tablenow.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.entity.TimeStamped;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor
public class Reservation extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime reservedAt;
    private LocalDateTime deletedAt;

    @Builder
    public Reservation(User user, Store store, LocalDateTime reservedAt) {
        this.user = user;
        this.store = store;
        this.reservedAt = reservedAt;
        this.status = ReservationStatus.RESERVED;
    }

    public void updateReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }

    public void tryCancel() {
        if (this.status == ReservationStatus.CANCELED) {
            throw new HandledException(ErrorCode.RESERVATION_ALREADY_CANCELED);
        }
        this.status = ReservationStatus.CANCELED;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateStatus(ReservationStatus newStatus) {
        if (this.status == newStatus) {
            throw new HandledException(ErrorCode.RESERVATION_STATUS_ALREADY_SET);
        }
        if (this.status == ReservationStatus.CANCELED) {
            throw new HandledException(ErrorCode.RESERVATION_ALREADY_CANCELED);
        }
        this.status = newStatus;
    }
}