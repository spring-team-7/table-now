package org.example.tablenow.domain.event.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event_join")
@NoArgsConstructor
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

    public void markNotified() {
        this.isNotified = true;
    }
}
