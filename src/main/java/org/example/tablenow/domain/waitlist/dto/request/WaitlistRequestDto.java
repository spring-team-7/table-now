package org.example.tablenow.domain.waitlist.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;


@Getter
@NoArgsConstructor
public class WaitlistRequestDto {
  @NotNull(message = "가게 ID를 입력하세요.")
  private Long storeId;
}
