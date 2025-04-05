package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationUpdateRequestDto {
    @NotNull
    private LocalDateTime reservedAt;
}
