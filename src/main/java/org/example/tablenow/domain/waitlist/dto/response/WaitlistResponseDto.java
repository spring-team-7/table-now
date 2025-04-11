package org.example.tablenow.domain.waitlist.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.waitlist.entity.Waitlist;

import java.time.LocalDateTime;

@Getter
public class WaitlistResponseDto {
    private final Long waitlistId;
    private final Long storeId;
    private final String storeName;
    private final boolean isNotified;
    private final LocalDateTime createdAt;

    @Builder
    public WaitlistResponseDto(Long waitlistId, Long storeId, String storeName, boolean isNotified, LocalDateTime createdAt) {
        this.waitlistId = waitlistId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.isNotified = isNotified;
        this.createdAt = createdAt;
    }

    public static WaitlistResponseDto fromWaitlist(Waitlist waitlist) {
        return WaitlistResponseDto.builder()
            .waitlistId(waitlist.getId())
            .storeId(waitlist.getStore().getId())
            .storeName(waitlist.getStore().getName())
            .isNotified(waitlist.getIsNotified())
            .createdAt(waitlist.getCreatedAt())
            .build();
    }
}

