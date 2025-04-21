package org.example.tablenow.domain.settlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;

@Getter
@AllArgsConstructor
public class SettlementOperationResponseDto {
    private int count;
    private SettlementStatus status;
}
