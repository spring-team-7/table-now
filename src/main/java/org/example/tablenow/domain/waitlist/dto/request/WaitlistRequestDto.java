package org.example.tablenow.domain.waitlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;


@Getter
public class WaitlistRequestDto {
  @NotNull(message = "가게 ID를 입력하세요.")
  private Long storeId;
}
