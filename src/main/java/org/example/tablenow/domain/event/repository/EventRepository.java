package org.example.tablenow.domain.event.repository;

import org.example.tablenow.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByStoreIdAndEventTime(Long storeId, LocalDateTime eventTime);
}
