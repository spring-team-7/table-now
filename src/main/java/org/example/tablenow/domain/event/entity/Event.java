package org.example.tablenow.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.global.entity.TimeStamped;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Entity
@Table(name = "event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private LocalDateTime endAt;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private int limitPeople;

    @Column(nullable = false)
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Builder
    private Event(Store store, String content, LocalDateTime openAt, LocalDateTime endAt, LocalDateTime eventTime, int limitPeople) {
        this.store = store;
        this.content = content;
        this.openAt = openAt;
        this.endAt = endAt;
        this.eventTime = eventTime;
        this.limitPeople = limitPeople;
        this.availableSeats = limitPeople;
        this.status = EventStatus.READY;
    }

    public void update(LocalDateTime openAt, LocalDateTime eventTime, Integer limitPeople) {
        if (openAt != null) this.openAt = openAt;
        if (eventTime != null) this.eventTime = eventTime;
        if (limitPeople != null) {
            this.limitPeople = limitPeople;
            this.availableSeats = limitPeople;
        }
    }

    public void decreaseAvailableSeats() {
        if (this.availableSeats > 0) {
            this.availableSeats--;
        } else {
            throw new HandledException(ErrorCode.EVENT_FULL);
        }
    }

    public boolean isReady() {
        return this.status == EventStatus.READY;
    }

    public void open() {
        changeStatus(EventStatus.OPENED);
    }

    public void close() {
        changeStatus(EventStatus.CLOSED);
    }

    public void validateOpenStatus() {
        if (this.status != EventStatus.OPENED) {
            throw new HandledException(ErrorCode.EVENT_NOT_OPENED);
        }
    }

    public static Event create(Store store, EventRequestDto dto) {
        return Event.builder()
                .store(store)
                .content(dto.getContent())
                .openAt(dto.getOpenAt())
                .endAt(dto.getEndAt())
                .eventTime(dto.getEventTime())
                .limitPeople(dto.getLimitPeople())
                .build();
    }

    public Long getStoreId() {
        return Optional.ofNullable(this.store)
                .map(Store::getId)
                .orElse(null);
    }

    public String getStoreName() {
        return Optional.ofNullable(this.store)
                .map(Store::getName)
                .orElse(null);
    }

    private void changeStatus(EventStatus status) {
        this.status = status;
    }
}
