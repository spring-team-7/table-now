package org.example.tablenow.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor
public class Reservation extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
     */

    private Long storeId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime reservedAt;
    private LocalDateTime deletedAt;

    @Builder
    public Reservation(Long userId, Long storeId, LocalDateTime reservedAt) {
        this.userId = userId;
        this.storeId = storeId;
        this.reservedAt = reservedAt;
        this.status = ReservationStatus.RESERVED;
    }

    public void updateReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }
}