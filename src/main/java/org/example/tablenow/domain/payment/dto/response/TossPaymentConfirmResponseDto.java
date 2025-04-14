package org.example.tablenow.domain.payment.dto.response;

import lombok.*;

@Getter
public class TossPaymentConfirmResponseDto {
    private final String paymentKey;
    private final String orderId;
    private final String status;
    private final String method;
    private final int totalAmount;

    @Builder
    public TossPaymentConfirmResponseDto(String paymentKey, String orderId, String status, String method, int totalAmount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.method = method;
        this.totalAmount = totalAmount;
    }
}
