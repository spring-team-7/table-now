package org.example.tablenow.domain.reservation.repository;

import jakarta.validation.constraints.NotNull;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByStoreIdAndReservedAt(Long storeId, LocalDateTime reservedAt);
    boolean existsByStoreIdAndReservedAtAndIdNot(Long storeId, @NotNull LocalDateTime reservedAt, Long id);
}
