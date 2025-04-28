package org.example.tablenow.domain.settlement.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettlementPageRequestDto {

    @Positive
    private int page = 1;
    @Positive
    private int size = 10;
}
