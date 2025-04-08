package org.example.tablenow.domain.event.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.Event;

@Getter
public class EventDeleteResponseDto {
    private final Long eventId;
    private final Long storeId;
    private final String storeName;
    private final String message;

    @Builder
    private EventDeleteResponseDto(Long eventId, Long storeId, String storeName, String message) {
        this.eventId = eventId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.message = message;
    }

    public static EventDeleteResponseDto fromEvent(Event event) {
        return EventDeleteResponseDto.builder()
                .eventId(event.getId())
                .storeId(event.getStore().getId())
                .storeName(event.getStore().getName())
                .message("이벤트 삭제에 성공했습니다.")
                .build();
    }
}
