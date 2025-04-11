package org.example.tablenow.domain.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TossPaymentCancelResponseDto {
    private final String paymentKey;
    private final String orderId;
    private final String status;
    private final String canceledAt;

    @Builder
    public TossPaymentCancelResponseDto(String paymentKey, String orderId, String status, String canceledAt) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.canceledAt = canceledAt;
    }
}
