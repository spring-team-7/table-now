package org.example.tablenow.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.global.annotation.HalfHourOnly;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {
    @NotNull
    private Long storeId;
    @NotNull
    @HalfHourOnly
    private LocalDateTime reservedAt;
}