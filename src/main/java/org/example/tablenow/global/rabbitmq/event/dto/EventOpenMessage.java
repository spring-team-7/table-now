package org.example.tablenow.global.rabbitmq.event.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.Event;

import java.time.LocalDateTime;

@Getter
public class EventOpenMessage {
    private final Long eventId;
    private final Long storeId;
    private final String storeName;
    private final LocalDateTime openAt;

    @Builder
    private EventOpenMessage(Long eventId, Long storeId, String storeName, LocalDateTime openAt) {
        this.eventId = eventId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.openAt = openAt;
    }

    public static EventOpenMessage fromEvent(Event event) {
        return EventOpenMessage.builder()
                .eventId(event.getId())
                .storeId(event.getStore().getId())
                .storeName(event.getStore().getName())
                .openAt(event.getOpenAt())
                .build();
    }
}
