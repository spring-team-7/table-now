package org.example.tablenow.domain.settlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class SettlementSummaryPageDto {

    private int totalAmount;
    private int doneAmount;
    private int readyAmount;
    private int canceledAmount;

    private Page<SettlementResponseDto> page;

    public static SettlementSummaryPageDto of(
            int doneAmount,
            int readyAmount,
            int canceledAmount,
            Page<SettlementResponseDto> page
    ) {

        int total = doneAmount + readyAmount + canceledAmount;

        return new SettlementSummaryPageDto(
                total,
                doneAmount,
                readyAmount,
                canceledAmount,
                page
        );
    }
}
