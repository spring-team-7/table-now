package org.example.tablenow.domain.event.repository;

import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventJoinRepository extends JpaRepository<EventJoin, Long> {
    boolean existsByUserAndEvent(User user, Event event);
    int countByEvent(Event event);

    @Query("SELECT ej.user FROM EventJoin ej WHERE ej.event.id = :eventId")
    List<User> findUsersByEventId(Long eventId);

}
