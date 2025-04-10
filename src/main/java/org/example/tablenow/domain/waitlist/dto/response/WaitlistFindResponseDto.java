package org.example.tablenow.domain.waitlist.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.waitlist.entity.Waitlist;

import java.time.LocalDateTime;

@Getter
public class WaitlistFindResponseDto {
  private final Long waitlistId;
  private final Long storeId;
  private final String storeName;
  private final LocalDateTime createdAt;

  @Builder
  public WaitlistFindResponseDto(Long waitlistId, Long storeId, String storeName, LocalDateTime createdAt) {
    this.waitlistId = waitlistId;
    this.storeId = storeId;
    this.storeName = storeName;
    this.createdAt = createdAt;
  }

  public static WaitlistFindResponseDto fromWaitlist(Waitlist waitlist) {
    return WaitlistFindResponseDto.builder()
        .waitlistId(waitlist.getId())
        .storeId(waitlist.getStore().getId())
        .storeName(waitlist.getStore().getName())
        .createdAt(waitlist.getCreatedAt())
        .build();
  }
}