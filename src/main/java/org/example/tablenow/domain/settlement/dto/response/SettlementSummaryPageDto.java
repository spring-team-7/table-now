package org.example.tablenow.domain.settlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class SettlementSummaryPageDto {

    private int totalAmount;
    private int doneAmount;
    private int pendingAmount;
    private int canceledAmount;

    private Page<SettlementResponseDto> page;

    public static SettlementSummaryPageDto of(
            Page<SettlementResponseDto> page,
            int doneAmount,
            int pendingAmount,
            int canceledAmount
    ) {
        int total = doneAmount + pendingAmount + canceledAmount;
        return new SettlementSummaryPageDto(total, doneAmount, pendingAmount, canceledAmount, page);
    }
}
