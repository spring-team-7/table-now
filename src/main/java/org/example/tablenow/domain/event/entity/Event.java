package org.example.tablenow.domain.event.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event")
@NoArgsConstructor
public class Event extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime openAt;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private int limitPeople;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Builder
    public Event(Store store, String content, LocalDateTime openAt, LocalDateTime eventTime, int limitPeople) {
        this.store = store;
        this.content = content;
        this.openAt = openAt;
        this.eventTime = eventTime;
        this.limitPeople = limitPeople;
        this.status = EventStatus.READY;
    }

    public void update(LocalDateTime openAt, LocalDateTime eventTime, Integer limitPeople) {
        if (openAt != null) this.openAt = openAt;
        if (eventTime != null) this.eventTime = eventTime;
        if (limitPeople != null) this.limitPeople = limitPeople;
    }

    public void changeStatus(EventStatus status) {
        this.status = status;
    }

    public static Event create(Store store, EventRequestDto dto) {
        return Event.builder()
                .store(store)
                .content(dto.getContent())
                .openAt(dto.getOpenAt())
                .eventTime(dto.getEventTime())
                .limitPeople(dto.getLimitPeople())
                .build();
    }

}
