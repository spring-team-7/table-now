package org.example.tablenow.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Entity
@Table(name = "event_join")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventJoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "is_notified", nullable = false)
    private boolean isNotified = false;

    @Builder
    public EventJoin(User user, Event event) {
        this.user = user;
        this.event = event;
        this.joinedAt = LocalDateTime.now();
        this.isNotified = false;
    }

    public Long getEventId() {
        return Optional.ofNullable(this.event)
                .map(Event::getId)
                .orElse(null);
    }

    public Long getStoreId() {
        return Optional.ofNullable(this.event)
                .map(Event::getStoreId)
                .orElse(null);
    }

    public String getStoreName() {
        return Optional.ofNullable(this.event)
                .map(Event::getStoreName)
                .orElse(null);
    }

    public LocalDateTime getEventTime() {
        return Optional.ofNullable(this.event)
                .map(Event::getEventTime)
                .orElse(null);
    }
}
