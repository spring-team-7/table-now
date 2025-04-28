package org.example.tablenow.domain.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;

import java.time.LocalDateTime;

@Getter
public class EventResponseDto {
    private final Long eventId;
    private final Long storeId;
    private final String storeName;
    private final String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime openAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime eventTime;
    private final int limitPeople;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedAt;
    private final EventStatus status;

    @Builder
    public  EventResponseDto(Long eventId, Long storeId, String storeName, String content, LocalDateTime openAt, LocalDateTime endAt,
                             LocalDateTime eventTime, int limitPeople, LocalDateTime createdAt, LocalDateTime updatedAt, EventStatus status) {
        this.eventId = eventId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.content = content;
        this.openAt = openAt;
        this.endAt = endAt;
        this.eventTime = eventTime;
        this.limitPeople = limitPeople;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static EventResponseDto fromEvent(Event event) {
        return EventResponseDto.builder()
                .eventId(event.getId())
                .storeId(event.getStoreId())
                .storeName(event.getStoreName())
                .content(event.getContent())
                .openAt(event.getOpenAt())
                .endAt(event.getEndAt())
                .eventTime(event.getEventTime())
                .limitPeople(event.getLimitPeople())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .status(event.getStatus())
                .build();
    }
}
