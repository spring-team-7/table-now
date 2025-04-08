package org.example.tablenow.domain.event.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.EventJoin;

import java.time.LocalDateTime;

@Getter
public class EventJoinResponseDto {
    private final Long eventJoinId;
    private final Long eventId;
    private final Long storeId;
    private final String storeName;
    private final LocalDateTime eventTime;
    private final LocalDateTime joinedAt;
    private final String message;

    @Builder
    private EventJoinResponseDto(Long eventJoinId, Long eventId, Long storeId,
                                String storeName, LocalDateTime eventTime, LocalDateTime joinedAt, String message) {
        this.eventJoinId = eventJoinId;
        this.eventId = eventId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.eventTime = eventTime;
        this.joinedAt = joinedAt;
        this.message = message;
    }

    public static EventJoinResponseDto fromEventJoin(EventJoin eventJoin) {
        return EventJoinResponseDto.builder()
                .eventJoinId(eventJoin.getId())
                .eventId(eventJoin.getEvent().getId())
                .storeId(eventJoin.getEvent().getStore().getId())
                .storeName(eventJoin.getEvent().getStore().getName())
                .eventTime(eventJoin.getEvent().getEventTime())
                .joinedAt(eventJoin.getJoinedAt())
                .message("이벤트 예약에 성공했습니다.")
                .build();
    }
}
