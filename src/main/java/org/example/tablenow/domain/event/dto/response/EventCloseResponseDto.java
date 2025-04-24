package org.example.tablenow.domain.event.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;

@Getter
public class EventCloseResponseDto {
    private final Long eventId;
    private final EventStatus status;
    private final String message;

    @Builder
    public EventCloseResponseDto(Long eventId, EventStatus status, String message) {
        this.eventId = eventId;
        this.status = status;
        this.message = message;
    }

    public static EventCloseResponseDto fromEvent(Event event) {
        return EventCloseResponseDto.builder()
                .eventId(event.getId())
                .status(event.getStatus())
                .message("이벤트가 성공적으로 종료되었습니다.")
                .build();
    }
}
