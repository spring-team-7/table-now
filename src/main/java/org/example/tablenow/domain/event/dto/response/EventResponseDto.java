package org.example.tablenow.domain.event.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventStatus;

import java.time.LocalDateTime;

@Getter
public class EventResponseDto {
    private final Long eventId;
    private final Long storeId;
    private final String storeName;
    private final String content;
    private final LocalDateTime openAt;
    private final LocalDateTime eventTime;
    private final int limitPeople;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final EventStatus status;

    @Builder
    private  EventResponseDto(Long eventId, Long storeId, String storeName, String content, LocalDateTime openAt,
                              LocalDateTime eventTime, int limitPeople, LocalDateTime createdAt, LocalDateTime updatedAt, EventStatus status) {
        this.eventId = eventId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.content = content;
        this.openAt = openAt;
        this.eventTime = eventTime;
        this.limitPeople = limitPeople;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static EventResponseDto fromEvent(Event event) {
        return EventResponseDto.builder()
                .eventId(event.getId())
                .storeId(event.getStore().getId())
                .storeName(event.getStore().getName())
                .content(event.getContent())
                .openAt(event.getOpenAt())
                .eventTime(event.getEventTime())
                .limitPeople(event.getLimitPeople())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .status(event.getStatus())
                .build();
    }
}
