package org.example.tablenow.domain.reservation.repository;

import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
    select count(r) > 0 from Reservation r
    where r.store.id = :storeId
      and r.reservedAt = :reservedAt
      and r.status <> 'CANCELED'
    """)
    boolean isReservedStatusInUse(Long storeId, LocalDateTime reservedAt);

    boolean existsByStoreIdAndReservedAtAndIdNot(Long storeId, LocalDateTime reservedAt, Long id);

    @Query("""
    SELECT r FROM Reservation r
    WHERE r.user.id = :userId
      AND (:status IS NULL OR r.status = :status)
    """)
    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    @Query("""
    SELECT r FROM Reservation r
    WHERE r.store.id = :storeId
      AND (:status IS NULL OR r.status = :status)
    """)
    Page<Reservation> findByStoreIdAndStatus(Long storeId, ReservationStatus status, Pageable pageable);

    @Query("""
SELECT r FROM Reservation r
WHERE r.status = 'RESERVED'
AND r.reservedAt BETWEEN :start AND :end
""")
  List<Reservation> findAllByReservedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.store.id = :storeId AND r.status = 'RESERVED'")
    long countReservedTables(@Param("storeId") Long storeId);
}
