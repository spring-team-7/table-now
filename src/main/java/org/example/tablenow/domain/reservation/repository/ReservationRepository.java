package org.example.tablenow.domain.reservation.repository;

import jakarta.validation.constraints.NotNull;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByStoreIdAndReservedAt(Long storeId, LocalDateTime reservedAt);
    boolean existsByStoreIdAndReservedAtAndIdNot(Long storeId, @NotNull LocalDateTime reservedAt, Long id);
//    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    // TODO: 유저 적용 후 r.userId -> r.user.id로 변경 필요
    @Query("""
    SELECT r FROM Reservation r
    WHERE r.userId = :userId 
      AND (:status IS NULL OR r.status = :status)
    """)
    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);
}
