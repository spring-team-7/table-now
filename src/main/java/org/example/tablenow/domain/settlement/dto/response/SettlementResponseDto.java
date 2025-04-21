package org.example.tablenow.domain.settlement.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.settlement.entity.Settlement;

import java.time.LocalDateTime;

@Getter
public class SettlementResponseDto {
    private final Long id;
    private final int amount;
    private final String status;
    private final Long paymentId;
    private final String storeName;
    private final String ownerName;
    private final LocalDateTime createdAt;

    @Builder
    public SettlementResponseDto(Long id, int amount, String status, Long paymentId, String storeName, String ownerName, LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.paymentId = paymentId;
        this.storeName = storeName;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
    }

    public static SettlementResponseDto fromSettlement(Settlement settlement) {
        return new SettlementResponseDto(
                settlement.getId(),
                settlement.getAmount(),
                settlement.getStatus().name(),
                settlement.getPayment().getId(),
                settlement.getPayment().getReservation().getStore().getName(),
                settlement.getPayment().getReservation().getStore().getUser().getName(),
                settlement.getCreatedAt()
        );
    }
}
