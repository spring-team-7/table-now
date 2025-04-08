package org.example.tablenow.domain.waitlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WaitlistResponseDto {
  private Long waitlistId;
  private Long storeId;
  private String storeName;
  private boolean isNotified;
  private LocalDateTime createdAt;
}

