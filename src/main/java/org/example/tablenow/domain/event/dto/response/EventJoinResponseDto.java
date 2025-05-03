package org.example.tablenow.domain.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.event.entity.EventJoin;

import java.time.LocalDateTime;

import static org.example.tablenow.global.constant.TimeConstants.TIME_YYYY_MM_DD_HH_MM_SS;

@Getter
public class EventJoinResponseDto {
    private final Long eventJoinId;
    private final Long eventId;
    private final Long storeId;
    private final String storeName;
    @JsonFormat(pattern = TIME_YYYY_MM_DD_HH_MM_SS)
    private final LocalDateTime eventTime;
    @JsonFormat(pattern = TIME_YYYY_MM_DD_HH_MM_SS)
    private final LocalDateTime joinedAt;
    private final String message;

    @Builder
    public EventJoinResponseDto(Long eventJoinId, Long eventId, Long storeId,
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
                .eventId(eventJoin.getEventId())
                .storeId(eventJoin.getStoreId())
                .storeName(eventJoin.getStoreName())
                .eventTime(eventJoin.getEventTime())
                .joinedAt(eventJoin.getJoinedAt())
                .message("이벤트 예약에 성공했습니다.")
                .build();
    }
}
