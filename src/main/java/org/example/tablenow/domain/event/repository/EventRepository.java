package org.example.tablenow.domain.event.repository;

import jakarta.persistence.LockModeType;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByStore_IdAndEventTime(Long storeId, LocalDateTime eventTime);
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
