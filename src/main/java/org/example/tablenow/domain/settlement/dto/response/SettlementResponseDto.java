package org.example.tablenow.domain.settlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tablenow.domain.settlement.entity.Settlement;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SettlementResponseDto {
    private final Long id;
    private final int amount;
    private final String status;
    private final Long paymentId;
    private final String storeName;
    private final String ownerName;
    private final LocalDateTime createdAt;

    public static SettlementResponseDto fromSettlement(Settlement settlement) {
        return new SettlementResponseDto(
                settlement.getId(),
                settlement.getAmount(),
                settlement.getStatusName(),
                settlement.getPaymentId(),
                settlement.getPaymentStoreName(),
                settlement.getPaymentUserName(),
                settlement.getCreatedAt()
        );
    }
}
