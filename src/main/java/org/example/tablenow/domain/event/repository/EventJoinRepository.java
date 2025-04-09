package org.example.tablenow.domain.event.repository;

import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJoinRepository extends JpaRepository<EventJoin, Long> {
    boolean existsByUserAndEvent(User user, Event event);
    int countByEvent(Event event);
}
